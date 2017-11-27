package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.text.ScriptTextFormat
import org.abimon.spiral.core.formats.text.SpiralTextFormat
import org.abimon.spiral.core.objects.scripting.WRD
import org.abimon.spiral.core.println
import org.abimon.spiral.core.wrd.LabelEntry
import org.abimon.spiral.core.wrd.TextEntry
import org.abimon.visi.io.DataSource
import java.io.OutputStream

object WRDFormat : SpiralFormat {
    override val name = "WRD"
    override val extension = "wrd"
    override val conversions: Array<SpiralFormat> = arrayOf(ScriptTextFormat, SpiralTextFormat)

    val COMMAND_OP_CODE = 0x2B1D
    val COMMAND_OP_CODE_HEX = COMMAND_OP_CODE.toString(16)

    val STRING_OP_CODE = 0x2B1E
    val STRING_OP_CODE_HEX = STRING_OP_CODE.toString(16)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return WRD(source).entries.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        } catch(negative: NegativeArraySizeException) {
        }
        return false
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val wrd = WRD(source)

        wrd.entries.forEach { entry ->
            val op = SpiralData.drv3OpCodes[entry.opCode]?.second ?: "0x${entry.opCode.toString(16)}"
            when(entry) {
                is LabelEntry -> output.println("$op|${wrd.cmds[0][entry.labelID]}")
                is TextEntry -> output.println("$op|${entry.id}")
                else -> {
                    try {
                        output.println("$op|${entry.cmdArguments.joinToString { wrd.cmds[1][it] }}")
                    } catch(aioob: ArrayIndexOutOfBoundsException) {
                        output.println("$op|${entry.rawArguments.joinToString { "raw:$it" }}")
                    }
                }
            }
        }

        output.println("")

        wrd.cmds.forEachIndexed { cmdType, cmdList -> cmdList.forEachIndexed { index, s -> output.println("0x${COMMAND_OP_CODE_HEX}|$cmdType, $index, $s") } }
        wrd.strings.forEach { str -> output.println("0x${STRING_OP_CODE_HEX}|$str") }

        return true
    }
}