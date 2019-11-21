package info.spiralframework.core.formats.scripting

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.errors.HopesPeakMissingGameException
import info.spiralframework.formats.game.hpa.HopesPeakDRGame
import info.spiralframework.formats.scripting.Lin
import info.spiralframework.formats.common.scripting.lin.LinTextScript
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.utils.use
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.data.OSLDrone
import org.parboiled.parserunners.BasicParseRunner
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream

object OpenSpiralLanguageFormat : ReadableSpiralFormat<OSLDrone>, WritableSpiralFormat {
    override val name: String = "OpenSpiralLangauge"
    override val extension: String = "osl"

    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<OSLDrone> {
        val parser = OpenSpiralLanguageParser { resourceName -> readContext?.dataContext?.invoke(resourceName)?.invoke()?.use(InputStream::readBytes) }
        val runner = BasicParseRunner<Any>(parser.OpenSpiralLanguage())
        val result = runner.run(String(source.use(InputStream::readBytes)))

        if (!result.matched)
            return FormatResult.Fail(this, 1.0)
        return FormatResult.Success(this, OSLDrone(parser, result.valueStack, name), 1.0)
    }

    override fun supportsWriting(context: SpiralContext, data: Any): Boolean = data is Lin

    override fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, stream: OutputStream): FormatWriteResponse {
        when (data) {
            is Lin -> {
                val game = writeContext?.game as? HopesPeakDRGame
                        ?: return FormatWriteResponse.FAIL(HopesPeakMissingGameException(context, writeContext?.game))

                val out = PrintStream(stream)
                out.println("OSL Script")
                out.println("Set Game Context to ${game.names.firstOrNull()
                        ?: "CRASH [REASON: $game HAS NO NAMES]"}") //TODO: Actually crash here :/
                out.println()
                //TODO: Use LinScript#format eventually
                data.entries.forEach { script ->
                    if (script is LinTextScript) {
                        out.println("Text|${script.text?.replace("\n", "\\n") ?: ""}")
                    } else {
                        out.println("${game.opCodes[script.opcode]?.first?.firstOrNull()
                                ?: "0x${script.opcode.toString(16)}"}|${script.rawArguments.joinToString()}")
                    }
                }

                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}