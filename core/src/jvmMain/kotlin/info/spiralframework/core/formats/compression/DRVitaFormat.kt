package info.spiralframework.core.formats.compression

import com.soywiz.krypto.sha256
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.toHexString
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.compression.decompressVita
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.readBytes
import java.util.*

object DRVitaFormat: ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "DrVita Compression"
    override val extension: String = "cmp"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<DataSource<*>>> {
        if (source.useInputFlow { flow -> flow.readUInt32LE() == info.spiralframework.formats.common.compression.DR_VITA_MAGIC } == true)
            return FormatResult.Success(Optional.empty(), 1.0)
        return FormatResult.Fail(1.0)
    }

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
            val cache = context.cacheShortTerm(context, "drvita:${data.sha256().toHexString()}")

            val output = cache.openOutputFlow()
            if (output == null) {
                //Cache has failed; store in memory
                cache.close()
                return FormatResult.Success(this, BinaryDataSource(decompressVita(data)), 1.0)
            } else {
                output.write(decompressVita(data))

                val result = FormatResult.Success<DataSource<*>>(this, cache, 1.0)
                result.release.add(cache)
                return result
            }
        } catch (iae: IllegalArgumentException) {
            return FormatResult.Fail(this, 1.0, iae)
        }
    }
}