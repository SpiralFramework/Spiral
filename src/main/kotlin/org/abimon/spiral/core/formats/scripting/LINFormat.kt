package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.UnsafeLin
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.Lin
import org.abimon.spiral.core.objects.scripting.lin.LinTextScript
import org.abimon.spiral.core.println
import java.io.InputStream
import java.io.OutputStream

object LINFormat : SpiralFormat {
    override val name = "LIN"
    override val extension = "lin"
    override val conversions: Array<SpiralFormat> = arrayOf(OpenSpiralLanguageFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            return Lin(game as? HopesPeakDRGame ?: return false, dataSource)?.entries?.isNotEmpty() == true
        } catch (illegal: IllegalArgumentException) {
        } catch (negative: NegativeArraySizeException) {
        }
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params)) return true

        val translateNames = params["lin:translateNames"]?.toString()?.toBoolean() ?: true

        val hpaGame = game as? HopesPeakDRGame ?: return false

        output.println("OSL Script")
        output.println("Set Game To ${hpaGame.names[0]}")

        val lin = UnsafeLin(hpaGame, dataSource)

        lin.entries.forEach { entry ->
            if (translateNames) {
                if (entry is LinTextScript)
                    output.println("${game.opCodes[entry.opCode]?.first?.firstOrNull()
                            ?: "0x${entry.opCode.toString(16)}"}|${entry.text?.replace("\n", "\\n") ?: "Hello, Null!"}")
                else
                    output.println("${game.opCodes[entry.opCode]?.first?.firstOrNull()
                            ?: "0x${entry.opCode.toString(16)}"}|${entry.rawArguments.joinToString()}")
            } else {
                if (entry is LinTextScript)
                    output.println("0x${entry.opCode.toString(16)}|${entry.text?.replace("\n", "\\n")
                            ?: "Hello, Null!"}")
                else
                    output.println("0x${entry.opCode.toString(16)}|${entry.rawArguments.joinToString()}")
            }
        }

        return true
    }
}