package info.spiralframework.core.common.formats.compression

import korlibs.crypto.sha256
import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt64BE
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cacheShortTerm
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormatOptionalResult
import info.spiralframework.core.common.formats.SpiralFormatReturnResult
import info.spiralframework.core.common.formats.buildFormatSuccess
import info.spiralframework.formats.common.compression.decompressCrilayla

public object CrilaylaCompressionFormat : ReadableSpiralFormat<DataSource<*>> {
    override val name: String = "CRILAYLA Compression"
    override val extension: String = "cmp"

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<DataSource<*>> {
        if (source.useInputFlow { flow -> flow.readInt64BE() == info.spiralframework.formats.common.compression.CRILAYLA_MAGIC }
                .getOrDefault(false))
            return buildFormatSuccess(Optional.empty(), 1.0)

        return KorneaResult.empty()
    }

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<DataSource<*>> {
        val data = source.useInputFlow { flow -> flow.readBytes() }.getOrBreak { return it.cast() }
        val cache = context.cacheShortTerm(context, "crilayla:${data.sha256().hexLower}")

        return cache.openOutputFlow()
            .flatMap { output ->
                @Suppress("DEPRECATION")
                decompressCrilayla(data).map { data ->
                    output.write(data)

                    buildFormatSuccess(cache, 1.0)
                }.doOnFailure {
                    cache.close()
                    output.close()
                }
            }.getOrElseRun {
                cache.close()

                decompressCrilayla(data).flatMap { decompressed ->
                    buildFormatSuccess(BinaryDataSource(decompressed), 1.0)
                }
            }
    }
}