package info.spiralframework.core.formats.scripting

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.FormatWriteResponse
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.errors.HopesPeakMissingGameException
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.game.hpa.HopesPeakDRGame
import info.spiralframework.formats.scripting.Lin
import info.spiralframework.formats.scripting.lin.LinTextScript
import info.spiralframework.formats.utils.DataContext
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

    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<OSLDrone> {
        val parser = OpenSpiralLanguageParser { resourceName -> context(resourceName)?.invoke()?.use(InputStream::readBytes) }
        val runner = BasicParseRunner<Any>(parser.OpenSpiralLanguage())
        val result = runner.run(String(source.use(InputStream::readBytes)))

        if (!result.matched)
            return FormatResult.Fail(this, 1.0)
        return FormatResult.Success(this, OSLDrone(parser, result.valueStack, name), 1.0)
    }

    override fun supportsWriting(data: Any): Boolean = data is Lin

    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): FormatWriteResponse {
        when (data) {
            is Lin -> {
                if (game !is HopesPeakDRGame)
                    return FormatWriteResponse.FAIL(HopesPeakMissingGameException(game))

                val out = PrintStream(stream)
                out.println("OSL Script")
                out.println("Set Game Context to ${game.names.firstOrNull() ?: "CRASH [REASON: $game HAS NO NAMES]"}") //TODO: Actually crash here :/
                out.println()
                //TODO: Use LinScript#format eventually
                data.entries.forEach { script ->
                    if (script is LinTextScript) {
                        out.println("Text|${script.text?.replace("\n", "\\n") ?: ""}")
                    } else {
                        out.println("${game.opCodes[script.opCode]?.first?.firstOrNull() ?: "0x${script.opCode.toString(16)}"}|${script.rawArguments.joinToString()}")
                    }
                }

                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}