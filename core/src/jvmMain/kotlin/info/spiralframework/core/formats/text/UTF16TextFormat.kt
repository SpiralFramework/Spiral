package info.spiralframework.core.formats.text

import dev.brella.kornea.errors.common.*
import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.manuallyEncode
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.text.toHexString

object UTF16TextFormat : ReadableSpiralFormat<String>, WritableSpiralFormat {
    override val name: String = "UTF-16 Text"
    override val extension: String = "txt"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<Optional<String>> =
        source.openInputFlow().flatMap { flow ->
            val bom = flow.readInt16LE()
            if (bom == 0xFFFE || bom == 0xFEFF) KorneaResult.success(Optional.empty<String>()) else KorneaResult.errorAsIllegalArgument(-1, "Invalid magic number $bom")
        }.buildFormatResult(1.0)

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<String> {
        val data = source.useInputFlow { flow -> flow.readBytes() }
            .getOrBreak { return it.cast() }

        val hasBom = (data[0] == 0xFF.toByte() && data[1] == 0xFE.toByte()) || (data[0] == 0xFE.toByte() && data[1] == 0xFF.toByte())
        val hasNullTerminator = data[data.size - 1] == 0x00.toByte() && data[data.size - 2] == 0x00.toByte()

        if (!hasBom)
            return KorneaResult.errorAsIllegalArgument(-1, "Invalid byte order marker ${data[0].toHexString()} ${data[1].toHexString()}")
        return buildFormatResult(String(data, Charsets.UTF_16).trimEnd('\u0000'), 1.0)
    }

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = true

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        flow.write(manuallyEncode(data.toString(), TextCharsets.UTF_16LE, true))
        return FormatWriteResponse.SUCCESS
    }
}