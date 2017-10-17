package org.abimon.spiral.core.formats

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.objects.WRD
import org.abimon.spiral.core.println
import org.abimon.spiral.core.wrd.LabelEntry
import org.abimon.visi.io.DataSource
import java.io.OutputStream

object WRDFormat : SpiralFormat {
    override val name = "WRD"
    override val extension = "wrd"
    override val conversions: Array<SpiralFormat> = arrayOf(TXTFormat, SpiralTextFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            return WRD(source).entries.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is TXTFormat || format is SpiralTextFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val wrd = WRD(source)

        wrd.entries.forEach { entry ->
//            if (entry is TextEntry)
//                output.println("${SpiralData.drv3OpCodes[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.text.replace("\n", "\\n")}")
//            else
            val op = SpiralData.drv3OpCodes[entry.opCode]?.second ?: "0x${entry.opCode.toString(16)}"
            when(entry) {
                is LabelEntry -> output.println("$op|${wrd.cmds[0][entry.labelID]}")
//                is SetFlagEntry -> output.println("$op|${wrd.cmds[1][entry.valueID]}, ${wrd.cmds[1][entry.flagID]}")
//                is ScriptEntry -> output.println("$op|${wrd.cmds[1][entry.scriptID]}, ${wrd.cmds[1][entry.labelID]}")
//                is SpeakerEntry -> output.println("$op|${wrd.cmds[1][entry.charID]}")
//                is VoiceLineEntry -> output.println("$op|${wrd.cmds[1][entry.voiceLine]}, ${wrd.cmds[1][entry.volumeControl]}")
//                else -> {
//                    val args = (0 until entry.rawArguments.size / 2).map { i -> wrd.cmds[1][entry.rawArguments[i * 2] or (entry.rawArguments[i * 2 + 1] shl 0)] }
//                    //output.println("$op|${entry.rawArguments.joinToString()}")
//                    output.println("$op|${args.joinToString()}")
//                }
                else -> {
                    try {
                        output.println("$op|${entry.cmdArguments.joinToString { wrd.cmds[1][it] }}")
                    } catch(aioob: ArrayIndexOutOfBoundsException) {
                        output.println("$op|${entry.rawArguments.joinToString()}")
                    }
                }
            }
        }

        output.println("\nCommands: ")

        wrd.cmds.forEachIndexed { cmdType, cmdList -> output.println("\t$cmdType: ${cmdList.mapIndexed { index, s -> "this[$index]: $s" }.joinToString("\n") { "\t$it" } }") }

        return true
    }
}