package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.WordScriptFile
import org.abimon.spiral.core.println
import java.io.InputStream
import java.io.OutputStream

object WRDFormat : SpiralFormat {
    override val name = "WRD"
    override val extension = "wrd"
    override val conversions: Array<SpiralFormat> = arrayOf(OpenSpiralLanguageFormat)

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

        output.println("OSL Script")
        output.println("Set Game To V3")

        //TODO: Re implement WRD conversion
        wrd.labels.forEach { s -> output.println("Word Command Label: $s") }
        wrd.strings.forEach { s -> output.println("Word Command String: $s") }
        wrd.parameters.forEach { s -> output.println("Word Command Parameter: $s") }

        wrd.entries.forEach { entrySet ->
            output.println(entrySet.joinToString("\n") { entry ->
                val commandEntries = game.opCodeCommandEntries[entry.opCode]
                val range = commandEntries?.indices ?: IntRange.EMPTY

                val arguments = entry.rawArguments.mapIndexed { index, arg ->
                    if (index in range) {
                        return@mapIndexed when (commandEntries!![index]) {
                            EnumWordScriptCommand.LABEL -> "LABEL:${wrd.labels[arg]}"
                            EnumWordScriptCommand.PARAMETER -> "PARAMETER:${wrd.parameters[arg]}"
                            EnumWordScriptCommand.STRING -> "STRING:${wrd.strings[arg]}"
                            EnumWordScriptCommand.RAW -> arg.toString()
                        }
                    } else {
                        return@mapIndexed arg.toString()
                    }
                }

                return@joinToString "0x${entry.opCode.toString(16)}|${arguments.joinToString()}"
            })

            output.println("")
        }

        return true
    }
}