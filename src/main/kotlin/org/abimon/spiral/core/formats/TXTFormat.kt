package org.abimon.spiral.core.formats

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.isDebug
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.UnknownEntry
import org.abimon.spiral.core.objects.CustomLin
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.make
import org.abimon.visi.lang.times
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream

//TODO: Support DR2 op codes too
object TXTFormat : SpiralFormat {
    override val name = "Text"
    override val extension = "txt"

    override fun isFormat(source: DataSource): Boolean = true

    override fun canConvert(format: SpiralFormat): Boolean = format is LINFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        if (isDebug) println("Begun Converting\n${"-" * 100}")
        when (format) {
            is LINFormat -> {
                source.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val lin = make<CustomLin> {
                        reader.forEachLine loop@ { line ->
                            val parts = line.split("|", limit = 2)
                            if (parts.size != 2) {
                                return@loop
                            }

                            val opCode = parts[0]

                            val op: Int
                            if (opCode.startsWith("0x"))
                                op = opCode.substring(2).toInt(16)
                            else if (opCode.matches("\\d+".toRegex()))
                                op = opCode.toInt()
                            else if (SpiralData.dr1OpCodes.values.any { (_, name) -> name.equals(opCode, true) })
                                op = SpiralData.dr1OpCodes.entries.first { (_, pair) -> pair.second.equals(opCode, true) }.key
                            else
                                op = 0x00

                            if (op == 2) { //Text
                                entry(parts[1])
                            } else {
                                val args = if (parts[1].isBlank()) IntArray(0) else parts[1].split(",").map(String::trim).map(String::toInt).toIntArray()
                                when (op) {
                                    0x00 -> entry(TextCountEntry((args[1] shl 8) or args[0]))
                                    else -> entry(UnknownEntry(op, args))
                                }
                            }
                        }
                    }
                    lin.compile(output)
                }
            }
        }
    }
}