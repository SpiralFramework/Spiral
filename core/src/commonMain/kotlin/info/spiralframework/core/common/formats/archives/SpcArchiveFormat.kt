package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.empty
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.locale.errorAsLocalisedIllegalArgument
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.archives.*

public object SpcArchiveFormat : ReadableSpiralFormat<SpcArchive>, WritableSpiralFormat<CustomSpcArchive> {
    override val name: String = "Spc"
    override val extension: String = "spc"

    override fun preferredConversionFormat(
        context: SpiralContext,
        properties: SpiralProperties?
    ): WritableSpiralFormat<*>? = ZipFormat

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<SpcArchive> =
        source.openInputFlow().useAndFlatMap { flow ->
            val magic = flow.readInt32LE()
                ?: return@useAndFlatMap context.localisedNotEnoughData(SpcArchive.NOT_ENOUGH_DATA_KEY)

            if (magic != SpcArchive.SPC_MAGIC_NUMBER_LE) {
                return@useAndFlatMap context.errorAsLocalisedIllegalArgument(
                    SpcArchive.INVALID_MAGIC_NUMBER,
                    SpcArchive.INVALID_MAGIC_NUMBER_KEY,
                    "0x${magic.toString(16)}",
                    "0x${SpcArchive.SPC_MAGIC_NUMBER_LE.toString(16)}"
                )
            }

            return@useAndFlatMap buildFormatSuccess(Optional.empty(), 1.0)
        }

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<SpcArchive> =
        SpcArchive(context, source)
            .filter { spc -> spc.files.isNotEmpty() }
            .ensureFormatSuccess { spc -> if (spc.files.size == 1) 0.75 else 1.0 }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is AwbArchive
                || data is WadArchive
                || data is CpkArchive
                || data is SpcArchive
                || data is PakArchive
//        || data is ZipFile

    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<CustomSpcArchive> {
        val customSpc = CustomSpcArchive()
        val caches: MutableList<DataPool<*, *>> = ArrayList()

        when (data) {
            is AwbArchive -> data.files.forEach { entry -> customSpc[entry.id.toString()] = data.openSource(entry) }
            is CpkArchive -> data.files.forEach { entry ->
                customSpc[entry.name, if (entry.isCompressed) SpcArchive.COMPRESSED_FLAG else 0, entry.fileSize.toLong(), entry.extractSize.toLong()] =
                    data.openRawSource(entry)
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
            else -> return KorneaResult.spiralWrongFormat()
        }

        customSpc.compile(flow)
        caches.suspendForEach(DataPool<*, *>::close)
        return KorneaResult.success(customSpc)
    }
}