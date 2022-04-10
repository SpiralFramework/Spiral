package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.locale.errorAsLocalisedIllegalArgument
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.archives.*


public object PakArchiveFormat : ReadableSpiralFormat<PakArchive>, WritableSpiralFormat<CustomPakArchive> {
    override val name: String = "Pak"
    override val extension: String = "pak"

    override fun preferredConversionFormat(
        context: SpiralContext,
        properties: SpiralProperties?
    ): WritableSpiralFormat<*> = ZipFormat

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<PakArchive> {
        val fileName = readContext[ISpiralProperty.FileName]?.substringAfterLast('/')
        if (fileName != null && fileName.contains('.')) {
            if (!fileName.substringAfterLast('.').equals(extension, true)) {
                return context.errorAsLocalisedIllegalArgument(
                    -1,
                    "Invalid extension ${fileName.substringAfterLast('.')}"
                )
            }
        }

        return super.identify(context, readContext, source)
    }

    /**
     * Attempts to read the data source as [T]
     *
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<PakArchive> =
        PakArchive(context, source)
            .filter { pak -> pak.files.isNotEmpty() }
            .ensureFormatSuccess { pak ->
                when {
                    readContext[ISpiralProperty.FileName]?.substringAfterLast('.')
                        ?.equals(extension, true) == true -> 0.9

                    pak.files.size == 1 -> 0.7
                    else -> 0.8
                }
            }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is AwbArchive ||
                data is WadArchive ||
                data is CpkArchive ||
                data is SpcArchive ||
                data is PakArchive
//        || data is ZipFile

    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<CustomPakArchive> {
        val customPak = CustomPakArchive()
        val caches: MutableList<DataPool<*, *>> = ArrayList()

        when (data) {
            is AwbArchive -> data.files.forEach { entry -> customPak[entry.id] = data.openSource(entry) }
            is CpkArchive -> data.files.forEach { entry ->
                data.openDecompressedSource(context, entry).doOnSuccess {
                    customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] = it
                }
            }
            is PakArchive -> data.files.forEach { entry -> customPak[entry.index] = data.openSource(entry) }
            is SpcArchive -> data.files.forEach { entry ->
                data.openDecompressedSource(context, entry).doOnSuccess {
                    customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] = it
                }
            }
            is WadArchive -> data.files.forEach { entry ->
                customPak[entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()] =
                    data.openSource(entry)
            }
//            is ZipFile -> data.entries().iterator().forEach { entry ->
//                val cache = context.cacheShortTerm(context, "zip:${entry.name}")
//                val index = entry.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()
//
//                @Suppress("DEPRECATION")
//                cache.openOutputFlow()
//                    .map { output ->
//                        data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
//                        customPak[index] = cache
//                        caches.add(cache)
//                    }.doOnFailure {
//                        cache.close()
//                        customPak[index] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
//                    }
//            }
            else -> return KorneaResult.spiralWrongFormat()
        }

        customPak.compile(flow)
        caches.suspendForEach(DataPool<*, *>::close)
        return KorneaResult.success(customPak)
    }
}