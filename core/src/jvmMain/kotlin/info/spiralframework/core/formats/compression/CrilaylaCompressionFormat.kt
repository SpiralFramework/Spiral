package info.spiralframework.core.formats.compression

import com.soywiz.krypto.sha256
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.toHexString
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.compression.decompressCrilayla
import org.abimon.kornea.errors.common.*
import org.abimon.kornea.io.common.BinaryDataSource
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt64BE
import org.abimon.kornea.io.common.useInputFlow
import java.util.*

object CrilaylaCompressionFormat : ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "CRILAYLA Compression"
    override val extension: String = "cmp"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<DataSource<*>>> {
        if (source.useInputFlow { flow -> flow.readInt64BE() == info.spiralframework.formats.common.compression.CRILAYLA_MAGIC }.getOrElse(false))
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
        val data = source.useInputFlow { flow -> flow.readBytes() }.getOrBreak { return FormatResult.Fail(this, 1.0, it) }
        val cache = context.cacheShortTerm(context, "crilayla:${data.sha256().toHexString()}")

        return cache.openOutputFlow()
            .flatMap { output ->
                @Suppress("DEPRECATION")
                decompressCrilayla(data).map { data ->
                    output.write(data)

                    val result = FormatResult.Success<DataSource<*>>(this, cache, 1.0)
                    result.release.add(cache)
                    result
                }.doOnFailure {
                    cache.close()
                    output.close()
                }
            }.getOrElseRun {
                cache.close()

                decompressCrilayla(data)
                    .map<ByteArray, FormatResult<DataSource<*>>> { decompressed -> FormatResult.Success(this, BinaryDataSource(decompressed), 1.0) }
                    .getOrElseTransform { failure -> FormatResult.Fail(this, 1.0, failure) }
            }
    }
}