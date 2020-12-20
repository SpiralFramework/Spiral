package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.useAndFlatMap
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.buildFormatResult
import info.spiralframework.formats.common.archives.*

object SpcArchiveFormat : ReadableSpiralFormat<SpcArchive>, WritableSpiralFormat {
    override val name: String = "Spc"
    override val extension: String = "spc"

    override fun preferredConversionFormat(): WritableSpiralFormat? = ZipFormat

    override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<SpcArchive>> =
        source.openInputFlow().useAndFlatMap { flow ->
            val magic = flow.readInt32LE() ?: return@useAndFlatMap context.localisedNotEnoughData(SpcArchive.NOT_ENOUGH_DATA_KEY)
            if (magic != SpcArchive.SPC_MAGIC_NUMBER_LE) {
                return@useAndFlatMap KorneaResult.errorAsIllegalArgument(SpcArchive.INVALID_MAGIC_NUMBER, context.localise(SpcArchive.INVALID_MAGIC_NUMBER_KEY, "0x${magic.toString(16)}", "0x${SpcArchive.SPC_MAGIC_NUMBER_LE.toString(16)}"))
            }

            return@useAndFlatMap buildFormatResult(Optional.empty(), 1.0)
        }

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param dataContext Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<SpcArchive> =
        SpcArchive(context, source)
            .filter { spc -> spc.files.isNotEmpty() }
            .buildFormatResult { spc -> if (spc.files.size == 1) 0.75 else 1.0 }

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
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
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param dataContext Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        val customSpc = CustomSpcArchive()
        val caches: MutableList<DataPool<*, *>> = ArrayList()

        when (data) {
            is AwbArchive -> data.files.forEach { entry -> customSpc[entry.id.toString()] = data.openSource(entry) }
            is CpkArchive -> data.files.forEach { entry ->
                customSpc[entry.name, if (entry.isCompressed) SpcArchive.COMPRESSED_FLAG else 0, entry.fileSize.toLong(), entry.extractSize.toLong()] =
                    data.openRawSource(context, entry)
            }
            is PakArchive -> data.files.forEach { entry -> customSpc[entry.index.toString()] = data.openSource(entry) }
            is SpcArchive -> data.files.forEach { entry ->
                data.openDecompressedSource(context, entry).doOnSuccess { customSpc[entry.name] = it }
            }
            is WadArchive -> data.files.forEach { entry -> customSpc[entry.name] = data.openSource(entry) }
//            is ZipFile -> data.entries().iterator().forEach { entry ->
//                val cache = context.cacheShortTerm(context, "zip:${entry.name}")
//
//                cache.openOutputFlow()
//                    .map { output ->
//                        data.getInputStream(entry).use { inStream -> JVMInputFlow(inStream, entry.name).copyTo(output) }
//                        customSpc[entry.name] = cache
//                        caches.add(cache)
//                    }.doOnFailure {
//                        cache.close()
//                        customSpc[entry.name] = BinaryDataSource(data.getInputStream(entry).use(InputStream::readBytes))
//                    }
//            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }

        customSpc.compile(flow)
        caches.suspendForEach(DataPool<*, *>::close)
        return FormatWriteResponse.SUCCESS
    }
}