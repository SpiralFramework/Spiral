package info.spiralframework.core.formats.compression

import com.soywiz.krypto.sha256
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.toHexString
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.compression.decompressCrilayla
import info.spiralframework.formats.common.compression.decompressVita
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.readBytes
import java.util.*

object DRVitaFormat : ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "DrVita Compression"
    override val extension: String = "cmp"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<DataSource<*>>> {
        if (source.useInputFlow { flow -> flow.readUInt32LE() == info.spiralframework.formats.common.compression.DR_VITA_MAGIC }.getOrElse(false))
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
        val cache = context.cacheShortTerm(context, "drvita:${data.sha256().toHexString()}")

        return cache.openOutputFlow()
            .flatMap { output ->
                @Suppress("DEPRECATION")
                decompressVita(data).map { data ->
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

                decompressVita(data)
                    .map<ByteArray, FormatResult<DataSource<*>>> { decompressed -> FormatResult.Success(this, BinaryDataSource(decompressed), 1.0) }
                    .getOrElseTransform { failure -> FormatResult.Fail(this, 1.0, failure) }
            }
    }
}