package info.spiralframework.core.common.formats.text

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.decodeToUTF8String
import dev.brella.kornea.io.common.encodeToUTF8ByteArray
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt24BE
import dev.brella.kornea.io.common.flow.readBytes
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.errorAsLocalisedIllegalArgument
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormatOptionalResult
import info.spiralframework.core.common.formats.SpiralFormatReturnResult
import info.spiralframework.core.common.formats.WritableSpiralFormat

public object StrictUtf8TextFormat : ReadableSpiralFormat<String>, WritableSpiralFormat<ByteArray> {
    override val name: String = "UTF-8 Text"
    override val extension: String = "txt"
    public const val BOM: Int = 0xEFBBBF

    override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): SpiralFormatOptionalResult<String> =
        source.openInputFlow().flatMap { flow ->
            val magic = flow.readInt24BE()
            if (magic == BOM)
                KorneaResult.success(Optional.empty<String>())
            else
                context.errorAsLocalisedIllegalArgument(-1, "Invalid magic number $magic")
        }.ensureFormatSuccess(1.0)

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): SpiralFormatReturnResult<String> =
        source.openInputFlow().flatMap { flow ->
            val magic = flow.readInt24BE()
            if (magic != BOM)
                return@flatMap context.errorAsLocalisedIllegalArgument(-1, "Invalid magic number $magic")

            KorneaResult.success(flow.readBytes().decodeToUTF8String().trimEnd('\u0000'))
        }.ensureFormatSuccess(1.0)

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = true

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): KorneaResult<ByteArray> {
        val encoded = data.toString().encodeToUTF8ByteArray()
        flow.write(encoded)

        return KorneaResult.success(encoded)
    }
}