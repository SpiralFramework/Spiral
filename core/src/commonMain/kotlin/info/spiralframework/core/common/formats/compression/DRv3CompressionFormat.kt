package info.spiralframework.core.common.formats.compression

import korlibs.crypto.sha256
import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.extensions.readInt32BE
import dev.brella.kornea.io.common.flow.readBytes
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.*
import info.spiralframework.formats.common.compression.decompressV3

public object DRv3CompressionFormat : ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "DRv3 Compression"
    override val extension: String = "cmp"

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<DataSource<*>> {
        if (source.useInputFlow { flow -> flow.readInt32BE() == info.spiralframework.formats.common.compression.DRV3_COMP_MAGIC_NUMBER }
                .getOrDefault(false))

            return buildFormatSuccess(Optional.empty(), 1.0)
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
    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<DataSource<*>> {
        val data = source.useInputFlow { flow -> flow.readBytes() }
            .getOrBreak { return it.cast() }

        val cache = context.cacheShortTerm(context, "drv3:${data.sha256().hexLower}")

        return cache.openOutputFlow()
            .flatMap { output ->
                @Suppress("DEPRECATION")
                decompressV3(context, data).map { data ->
                    output.write(data)

                    buildFormatSuccess(cache, 1.0)
                }.doOnFailure {
                    cache.close()
                    output.close()
                }
            }.getOrElseRun {
                cache.close()

                decompressV3(context, data).flatMap { decompressed ->
                    buildFormatSuccess(BinaryDataSource(decompressed), 1.0)
                }
            }
    }
}