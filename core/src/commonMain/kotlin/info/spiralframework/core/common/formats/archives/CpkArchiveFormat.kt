package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.archives.*

public object CpkArchiveFormat : ReadableSpiralFormat<CpkArchive>, WritableSpiralFormat<CustomCpkArchive> {
    override val name: String = "Cpk"
    override val extension: String = "cpk"

    /**
     * Attempts to read the data source as [T]
     *
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): SpiralFormatReturnResult<CpkArchive> =
        CpkArchive(context, source)
            .filter { cpk -> cpk.files.isNotEmpty() }
            .ensureFormatSuccess { cpk -> if (cpk.files.size == 1) 0.75 else 1.0 }

    /**
     * Does this format support writing [data]?
     *
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is AwbArchive
        || data is WadArchive
        || data is CpkArchive
        || data is SpcArchive
        || data is PakArchive
//        || data is ZipFile

    /**
     * Writes [data] to [flow] in this format
     *
     * @param data The data to wrote
     * @param flow The flow to write to
     *
     * @return An enum for the success of the operation
     */
    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): KorneaResult<CustomCpkArchive> {
        val customCpk = CustomCpkArchive()
        val caches: MutableList<DataPool<*, *>> = ArrayList()
        when (data) {
            is AwbArchive -> data.files.forEach { entry -> customCpk[entry.id.toString()] = data.openSource(entry) }
            is CpkArchive -> data.files.forEach { entry ->
                data.openDecompressedSource(context, entry).doOnSuccess { customCpk[entry.name] = it }
            }
            is PakArchive -> data.files.forEach { entry -> customCpk[entry.index.toString()] = data.openSource(entry) }
            is SpcArchive -> data.files.forEach { entry ->
                data.openDecompressedSource(context, entry).doOnSuccess { customCpk[entry.name] = it }
            }
            is WadArchive -> data.files.forEach { entry -> customCpk[entry.name] = data.openSource(entry) }
//            is ZipFile -> data.entries().iterator().forEach { entry ->
//                val cache = context.cacheShortTerm(context, "zip:${entry.name}")
//
//                cache.openOutputFlow()
//                    .map { output ->
//                        data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
//                        customCpk[entry.name] = cache
//                        caches.add(cache)
//                    }.switchIfFailure {
//                        cache.close()
//                        customCpk[entry.name] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
//
//                        KorneaResult.empty()
//                    }
//            }
            else -> return KorneaResult.failure(SpiralFormatError.WrongFormat)
        }

        customCpk.compile(context, flow)
        caches.suspendForEach(DataPool<*, *>::close)
        return KorneaResult.success(customCpk)
    }
}