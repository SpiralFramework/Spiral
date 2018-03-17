package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.text.ScriptTextFormat
import org.abimon.spiral.core.formats.text.SpiralTextFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.WordScriptFile
import org.abimon.spiral.core.objects.scripting.wrd.LabelEntry
import org.abimon.spiral.core.objects.scripting.wrd.TextEntry
import org.abimon.spiral.core.println
import java.io.InputStream
import java.io.OutputStream

object WRDFormat : SpiralFormat {
    override val name = "WRD"
    override val extension = "wrd"
    override val conversions: Array<SpiralFormat> = arrayOf(ScriptTextFormat, SpiralTextFormat)

    val COMMAND_OP_CODE = 0x2B1D
    val COMMAND_OP_CODE_HEX = COMMAND_OP_CODE.toString(16)

    val STRING_OP_CODE = 0x2B1E
    val STRING_OP_CODE_HEX = STRING_OP_CODE.toString(16)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            return game === V3 && WordScriptFile(game, dataSource).entries.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        } catch(negative: NegativeArraySizeException) {
        }
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true

        val wrd = WordScriptFile(game as? V3 ?: return false, dataSource)

        wrd.entries.forEach { entry ->
            val op = SpiralData.drv3OpCodes[entry.opCode]?.second ?: "0x${entry.opCode.toString(16)}"
            when(entry) {
                is LabelEntry -> output.println("$op|${wrd.commandOneEntries[entry.labelID]}")
                is TextEntry -> output.println("$op|${entry.id}")
                else -> {
                    try {
                        output.println("$op|${entry.cmdArguments.joinToString { arg -> wrd.commandTwoEntries[arg] }}")
                    } catch(aioob: ArrayIndexOutOfBoundsException) {
                        output.println("$op|${entry.rawArguments.joinToString { arg -> "raw:$arg" }}")
                    }
                }
            }
        }

        output.println("")

        wrd.commandOneEntries.forEach { s -> output.println("Word Command 1: $s") }
        wrd.commandTwoEntries.forEach { s -> output.println("Word Command 2: $s") }
        wrd.commandThreeEntries.forEach { s -> output.println("Word Command 3: $s") }
        wrd.strings.forEach { str -> output.println("Word String: $str") }

        return true
    }
}