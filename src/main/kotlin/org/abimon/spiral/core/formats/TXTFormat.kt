package org.abimon.spiral.core.formats

import org.abimon.spiral.core.TripleHashMap
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.UnknownEntry
import org.abimon.spiral.core.objects.CustomLin
import org.abimon.spiral.core.objects.CustomWRD
import org.abimon.spiral.util.debug
import org.abimon.spiral.util.shortToIntPair
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.make
import org.abimon.visi.lang.times
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream

object TXTFormat : SpiralFormat {
    override val name = "Text"
    override val extension = "txt"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat, WRDFormat)

    override fun isFormat(source: DataSource): Boolean = true

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

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

                            val opCode = parts[0]

                            val op: Int
                            if (opCode.startsWith("0x"))
                                op = opCode.substring(2).toInt(16)
                            else if (opCode.matches("\\d+".toRegex()))
                                op = opCode.toInt()
                            else if (ops.values.any { (_, name) -> name.equals(opCode, true) })
                                op = ops.entries.first { (_, pair) -> pair.second.equals(opCode, true) }.key
                            else
                                return@loop

                            if (op == 2) { //Text
                                entry(parts[1])
                            } else {
                                val args = if (parts.size == 1 || parts[1].isBlank()) IntArray(0) else parts[1].split(",").map(String::trim).map(String::toInt).toIntArray()
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
            is WRDFormat -> {
                source.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val wrd = make<CustomWRD> {
                        reader.forEachLine loop@ { line ->
                            val parts = line.split("|", limit = 2)

                            val opCode = parts[0]

                            val op: Int
                            if (opCode.startsWith("0x"))
                                op = opCode.substring(2).toInt(16)
                            else if (opCode.matches("\\d+".toRegex()))
                                op = opCode.toInt()
                            else if (SpiralData.drv3OpCodes.values.any { (_, name) -> name.equals(opCode, true) })
                                op = SpiralData.drv3OpCodes.entries.first { (_, pair) -> pair.second.equals(opCode, true) }.key
                            else
                                return@loop

//                            if (op == 2) { //Text
//                                entry(parts[1])
//                            } else {
//                                val args = if (parts.size == 1 || parts[1].isBlank()) IntArray(0) else parts[1].split(",").map(String::trim).map(String::toInt).toIntArray()
//                                when (op) {
//                                    0x00 -> entry(TextCountEntry((args[1] shl 8) or args[0]))
//                                    else -> entry(UnknownEntry(op, args))
//                                }
//                            }

                            if(op == WRDFormat.COMMAND_OP_CODE) {
                                val components = parts[1].split(',')
                                val cmd = components[0].trim().toIntOrNull() ?: 0
                                val index = components[1].trim().toIntOrNull() ?: 0
                                val value = components[2].trim()

                                command(cmd, value)
                            } else if(op == WRDFormat.STRING_OP_CODE) {
                                string(parts[1].trim())
                            } else {
                                if (parts.size == 1 || parts[1].isBlank())
                                    entry(org.abimon.spiral.core.wrd.UnknownEntry(op, IntArray(0)))
                                else {
                                    val args: MutableList<Int> = ArrayList()

                                    for (arg in parts[1].split(',').map(String::trim)) {
                                        if (arg.startsWith("raw:"))
                                            args.add(arg.split(':')[1].trim().toIntOrNull() ?: 0)
                                        else if(op == 0x46) {
                                            val (a, b) = shortToIntPair(arg.trim().toIntOrNull() ?: 0, true, false)

                                            args.add(a)
                                            args.add(b)
                                        } else {
                                            val index = command(if(op == 0x14) 0 else 1, arg.trim())
                                            val (a, b) = shortToIntPair(index, true, false)

                                            args.add(a)
                                            args.add(b)
                                        }
                                    }

                                    entry(org.abimon.spiral.core.wrd.UnknownEntry(op, args.toIntArray()))
                                }
                            }
                        }
                    }

                    wrd.compile(output)
                }
            }
        }

        return true
    }
}