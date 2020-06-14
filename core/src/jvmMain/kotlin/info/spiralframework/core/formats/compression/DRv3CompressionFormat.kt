package info.spiralframework.core.formats.compression

import com.soywiz.krypto.sha256
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.toHexString
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.compression.decompressCrilayla
import info.spiralframework.formats.common.compression.decompressV3
import org.abimon.kornea.errors.common.*
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.readBytes
import java.util.*

object DRv3CompressionFormat: ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "DRv3 Compression"
    override val extension: String = "cmp"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<DataSource<*>>> {
        if (source.useInputFlow { flow -> flow.readInt32BE() == info.spiralframework.formats.common.compression.DRV3_COMP_MAGIC_NUMBER }.getOrElse(false))
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
            val cache = context.cacheShortTerm(context, "drv3:${data.sha256().toHexString()}")

        return cache.openOutputFlow()
            .flatMap { output ->
                @Suppress("DEPRECATION")
                decompressV3(context, data).map { data ->
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

                decompressV3(context, data)
                    .map<ByteArray, FormatResult<DataSource<*>>> { decompressed -> FormatResult.Success(this, BinaryDataSource(decompressed), 1.0) }
                    .getOrElseTransform { failure -> FormatResult.Fail(this, 1.0, failure) }
            }
    }
}