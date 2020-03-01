package info.spiralframework.core.formats.compression

import com.soywiz.krypto.sha256
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.toHexString
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.compression.decompressCrilayla
import org.abimon.kornea.io.common.BinaryDataSource
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.useInputFlow

object CrilaylaCompressionFormat: ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "CRILAYLA Compression"
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
            val data = source.useInputFlow { flow -> flow.readBytes() } ?: return FormatResult.Fail(this, 1.0)
            val cache = context.cacheShortTerm(context, "crilayla:${data.sha256().toHexString()}")

            val output = cache.openOutputFlow()
            if (output == null) {
                //Cache has failed; store in memory
                cache.close()
                return FormatResult.Success(this, BinaryDataSource(decompressCrilayla(data)), 1.0)
            } else {
                output.write(decompressCrilayla(data))

                val result = FormatResult.Success<DataSource<*>>(this, cache, 1.0)
                result.release.add(cache)
                return result
            }
        } catch (iae: IllegalArgumentException) {
            return FormatResult.Fail(this, 1.0, iae)
        }
    }
}