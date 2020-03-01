package info.spiralframework.core.formats.compression

import com.soywiz.krypto.sha256
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.toHexString
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.archives.SpcArchive
import info.spiralframework.formats.common.archives.SpcFileEntry
import info.spiralframework.formats.common.compression.SPC_COMPRESSION_MAGIC_NUMBER
import info.spiralframework.formats.common.compression.decompressSpcData
import info.spiralframework.formats.common.games.DrGame
import org.abimon.kornea.io.common.BinaryDataSource
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.PeekableInputFlow
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.useInputFlow

data class SpcEntryFormatReadContextdata(val entry: SpcFileEntry?, override val name: String? = null, override val game: DrGame? = null): FormatReadContext

object SpcCompressionFormat: ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "SPC Compression"
    override val extension: String = "cmp"

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<DataSource<*>> {
        try {
            val data = source.useInputFlow { flow ->
                val entry = (readContext as? SpcEntryFormatReadContextdata)?.entry

                if (entry == null) {
                    require(flow.readInt32LE() == SPC_COMPRESSION_MAGIC_NUMBER)
                } else if (entry.compressionFlag != SpcArchive.COMPRESSED_FLAG) {
                    return@useInputFlow null
                }

                flow.readBytes()
            } ?: return FormatResult.Fail(this, 1.0)
            val cache = context.cacheShortTerm(context, "spc:${data.sha256().toHexString()}")

            val output = cache.openOutputFlow()
            if (output == null) {
                //Cache has failed; store in memory
                cache.close()
                return FormatResult.Success(this, BinaryDataSource(decompressSpcData(data)), 1.0)
            } else {
                output.write(decompressSpcData(data))

                val result = FormatResult.Success<DataSource<*>>(this, cache, 1.0)
                result.release.add(cache)
                return result
            }
        } catch (iae: IllegalArgumentException) {
            return FormatResult.Fail(this, 1.0, iae)
        }
    }
}