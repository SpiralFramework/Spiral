package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.text.ScriptTextFormat
import org.abimon.spiral.core.formats.text.SpiralTextFormat
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.scripting.Lin
import org.abimon.spiral.core.println
import org.abimon.spiral.util.InputStreamFuncDataSource
import java.io.InputStream
import java.io.OutputStream

object LINFormat : SpiralFormat {
    override val name = "LIN"
    override val extension = "lin"
    override val conversions: Array<SpiralFormat> = arrayOf(ScriptTextFormat, SpiralTextFormat)

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
        try {
            return Lin(InputStreamFuncDataSource(dataSource)).entries.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        } catch(negative: NegativeArraySizeException) {
        }
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, dataSource, output, params)) return true

        val dr1 = game == DR1
        Lin(InputStreamFuncDataSource(dataSource), dr1).entries.forEach { entry ->
            if (entry is TextEntry)
                output.println("${(if (dr1) SpiralData.dr1OpCodes else SpiralData.dr2OpCodes)[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.text.replace("\n", "\\n")}")
            else
                output.println("${(if (dr1) SpiralData.dr1OpCodes else SpiralData.dr2OpCodes)[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.getRawArguments().joinToString()}")
        }

        return true
    }
}