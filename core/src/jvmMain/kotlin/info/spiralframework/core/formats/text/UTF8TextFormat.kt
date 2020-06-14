package info.spiralframework.core.formats.text

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.manuallyEncode
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import org.abimon.kornea.errors.common.filterToInstance
import org.abimon.kornea.errors.common.getOrBreak
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.useInputFlow
import java.util.*

object UTF8TextFormat : ReadableSpiralFormat<String>, WritableSpiralFormat {
    override val name: String = "UTF-8 Text"
    override val extension: String = "txt"
    const val MAGIC_NUMBER = 0x38465455

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<String>> {
        val magic = source.useInputFlow { flow -> flow.readInt32LE() }
            .filterToInstance<Int>()
            .getOrBreak { return FormatResult.Fail(1.0, it) }

        if (magic == MAGIC_NUMBER) return FormatResult.Success(Optional.empty(), 1.0)
        else return FormatResult.Fail(0.75)
    }

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<String> =
            source.useInputFlow { flow ->
                val magic = flow.readInt32LE()
                if (magic != MAGIC_NUMBER) return@useInputFlow FormatResult.Fail<String>(1.0)
                FormatResult.Success(String(flow.readBytes(), Charsets.UTF_8).trimEnd('\u0000'), 1.0)
            }.getOrBreak { return FormatResult.Fail(1.0, it) }

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = true

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        flow.write(data.toString().toByteArray(Charsets.UTF_8))
        return FormatWriteResponse.SUCCESS
    }
}