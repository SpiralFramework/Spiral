package org.abimon.spiral.core.formats

import org.abimon.spiral.core.TripleHashMap
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.debug
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.UnknownEntry
import org.abimon.spiral.core.objects.CustomLin
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.make
import org.abimon.visi.lang.times
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream

object TXTFormat : SpiralFormat {
    override val name = "Text"
    override val extension = "txt"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat)

    override fun isFormat(source: DataSource): Boolean = true

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)

        debug("Begun Converting\n${"-" * 100}")
        when (format) {
            is LINFormat -> {
                val dr1 = "${params["lin:dr1"] ?: true}".toBoolean()
                val ops: TripleHashMap<Int, Int, String> = if(dr1) SpiralData.dr1OpCodes else SpiralData.dr2OpCodes

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
                            else if (ops.values.any { (_, name) -> name.equals(opCode, true) })
                                op = ops.entries.first { (_, pair) -> pair.second.equals(opCode, true) }.key
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