package info.spiralframework.core.common.formats.compression

import com.soywiz.krypto.sha256
import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.empty
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.archives.SpcArchive
import info.spiralframework.formats.common.archives.SpcFileEntry
import info.spiralframework.formats.common.compression.SPC_COMPRESSION_MAGIC_NUMBER
import info.spiralframework.formats.common.compression.decompressSpcData
import info.spiralframework.formats.common.games.DrGame

public data class SpcEntryFormatReadContextdata(
    val entry: SpcFileEntry?,
    override val name: String? = null,
    override val game: DrGame? = null
) : FormatReadContext

public object SpcCompressionFormat : ReadableSpiralFormat<DataSource<*>> {
    public const val NOT_SPC_DATA: Int = 0x1000
    public const val NOT_COMPRESSED: Int = 0x1001

    public const val NOT_SPC_DATA_KEY: String = "formats.compression.spc.not_spc_data"
    public const val NOT_COMPRESSED_KEY: String = "formats.compression.spc.not_compressed"

    override val name: String = "SPC Compression"
    override val extension: String = "cmp"

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<DataSource<*>> {
        //TODO: Double check
        if ((readContext as? SpcEntryFormatReadContextdata)?.entry?.compressionFlag == SpcArchive.COMPRESSED_FLAG)
            return buildFormatSuccess(Optional.empty(), 1.0)

        if (source.useInputFlow { flow -> flow.readInt32LE() == SPC_COMPRESSION_MAGIC_NUMBER }
                .getOrDefault(false))
            return buildFormatSuccess(Optional.empty(), 1.0)
        return KorneaResult.empty()
    }

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<DataSource<*>> {
        val data = source.useInputFlow { flow ->
            val entry = (readContext as? SpcEntryFormatReadContextdata)?.entry

            if (entry == null) {
                if (flow.readInt32LE() != SPC_COMPRESSION_MAGIC_NUMBER)
                    return@useInputFlow KorneaResult.errorAsIllegalArgument<ByteArray>(
                        NOT_SPC_DATA,
                        context.localise(NOT_SPC_DATA_KEY)
                    )
            } else if (entry.compressionFlag != SpcArchive.COMPRESSED_FLAG) {
                return@useInputFlow KorneaResult.errorAsIllegalArgument<ByteArray>(
                    NOT_COMPRESSED,
                    context.localise(NOT_COMPRESSED_KEY)
                )
            }

            KorneaResult.success(flow.readBytes())
        }.flatten().getOrBreak { return it.cast() }

        val cache = context.cacheShortTerm(context, "spc:${data.sha256().hexLower}")

        return cache.openOutputFlow()
            .flatMap { output ->
                decompressSpcData(data).map { data ->
                    output.write(data)
                    buildFormatSuccess(cache, 1.0)
                }.doOnFailure {
                    cache.close()
                    output.close()
                }
            }.getOrElseRun {
                cache.close()

                decompressSpcData(data).flatMap { decompressed ->
                    buildFormatSuccess(BinaryDataSource(decompressed), 1.0)
                }
            }
    }
}