package info.spiralframework.core.common.formats.text

import dev.brella.kornea.errors.common.*
import info.spiralframework.base.common.SpiralContext
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt24BE
import dev.brella.kornea.io.common.flow.readBytes
import info.spiralframework.base.binding.decodeToUTF8String
import info.spiralframework.base.binding.encodeToUTF8ByteArray
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat

object StrictUtf8TextFormat : ReadableSpiralFormat<String>, WritableSpiralFormat {
    override val name: String = "UTF-8 Text"
    override val extension: String = "txt"
    const val BOM = 0xEFBBBF

    override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<String>> =
        source.openInputFlow().flatMap { flow ->
            val magic = flow.readInt24BE()
            if (magic == BOM) KorneaResult.success(Optional.empty<String>()) else KorneaResult.errorAsIllegalArgument(-1, "Invalid magic number $magic")
        }.buildFormatResult(1.0)

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<String> =
        source.openInputFlow().flatMap { flow ->
            val magic = flow.readInt24BE()
            if (magic != BOM) return@flatMap KorneaResult.errorAsIllegalArgument(-1, "Invalid magic number $magic")
            KorneaResult.success(flow.readBytes().decodeToUTF8String().trimEnd('\u0000'))
        }.buildFormatResult(1.0)

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = true

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        flow.write(data.toString().encodeToUTF8ByteArray())
        return FormatWriteResponse.SUCCESS
    }
}