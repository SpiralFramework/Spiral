package info.spiralframework.core.formats.text

import dev.brella.kornea.errors.common.*
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt24BE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.readBytes

object StrictUtf8TextFormat : ReadableSpiralFormat<String>, WritableSpiralFormat {
    override val name: String = "UTF-8 Text"
    override val extension: String = "txt"
    const val BOM = 0xEFBBBF

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<Optional<String>> =
        source.openInputFlow().flatMap { flow ->
            val magic = flow.readInt24BE()
            if (magic == BOM) KorneaResult.success(Optional.empty<String>()) else KorneaResult.errorAsIllegalArgument(-1, "Invalid magic number $magic")
        }.buildFormatResult(1.0)

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<String> =
        source.openInputFlow().flatMap { flow ->
            val magic = flow.readInt24BE()
            if (magic != BOM) return@flatMap KorneaResult.errorAsIllegalArgument(-1, "Invalid magic number $magic")
            KorneaResult.success(String(flow.readBytes(), Charsets.UTF_8).trimEnd('\u0000'))
        }.buildFormatResult(1.0)

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = true

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        flow.write(data.toString().toByteArray(Charsets.UTF_8))
        return FormatWriteResponse.SUCCESS
    }
}