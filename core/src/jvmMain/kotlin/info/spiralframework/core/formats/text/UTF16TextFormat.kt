package info.spiralframework.core.formats.text

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.manuallyEncode
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.readInt16LE
import dev.brella.kornea.io.common.useInputFlow
import java.util.*

object UTF16TextFormat : ReadableSpiralFormat<String>, WritableSpiralFormat {
    override val name: String = "UTF-16 Text"
    override val extension: String = "txt"

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<String>> {
        val bom = source.useInputFlow { flow -> flow.readInt16LE() }
            .filterToInstance<Int>()
            .getOrBreak { return FormatResult.Fail(1.0, it) }

        if (bom == 0xFFFE || bom == 0xFEFF) return FormatResult.Success(Optional.empty(), 1.0)
        else return FormatResult.Fail(0.75)
    }

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<String> {
        val data = source.useInputFlow { flow -> flow.readBytes() }
            .getOrBreak { return FormatResult.Fail(1.0, it) }

        val hasBom = (data[0] == 0xFF.toByte() && data[1] == 0xFE.toByte()) || (data[0] == 0xFE.toByte() && data[1] == 0xFF.toByte())
        val hasNullTerminator = data[data.size - 1] == 0x00.toByte() && data[data.size - 2] == 0x00.toByte()

        if (!hasBom)
            return FormatResult.Fail(if (hasNullTerminator) 0.75 else 1.0)
        return FormatResult.Success(String(data, Charsets.UTF_16).trimEnd('\u0000'), 1.0)
    }

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = true

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        flow.write(manuallyEncode(data.toString(), TextCharsets.UTF_16LE, true))
        return FormatWriteResponse.SUCCESS
    }
}