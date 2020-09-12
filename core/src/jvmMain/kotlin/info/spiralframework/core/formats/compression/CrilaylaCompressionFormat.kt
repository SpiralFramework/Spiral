package info.spiralframework.core.formats.compression

import com.soywiz.krypto.sha256
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt64BE
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.toHexString
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.buildFormatResult
import info.spiralframework.formats.common.compression.decompressCrilayla

object CrilaylaCompressionFormat : ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "CRILAYLA Compression"
    override val extension: String = "cmp"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<Optional<DataSource<*>>> {
        if (source.useInputFlow { flow -> flow.readInt64BE() == info.spiralframework.formats.common.compression.CRILAYLA_MAGIC }.getOrElse(false))
            return buildFormatResult(Optional.empty(), 1.0)
        return KorneaResult.empty()
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
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<DataSource<*>> {
        val data = source.useInputFlow { flow -> flow.readBytes() }.getOrBreak { return it.cast() }
        val cache = context.cacheShortTerm(context, "crilayla:${data.sha256().toHexString()}")

        return cache.openOutputFlow()
            .flatMap { output ->
                @Suppress("DEPRECATION")
                decompressCrilayla(data).map { data ->
                    output.write(data)

                    buildFormatResult(cache, 1.0)
                }.doOnFailure {
                    cache.close()
                    output.close()
                }
            }.getOrElseRun {
                cache.close()

                decompressCrilayla(data).flatMap { decompressed ->
                    buildFormatResult(BinaryDataSource(decompressed), 1.0)
                }
            }
    }
}