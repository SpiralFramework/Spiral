package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.TripleHashMap
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.LINFormat
import org.abimon.spiral.core.formats.scripting.WRDFormat
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.UnknownEntry
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.CustomLin
import org.abimon.spiral.core.objects.scripting.CustomWRD
import org.abimon.spiral.util.debug
import org.abimon.spiral.util.shortToIntPair
import org.abimon.visi.lang.make
import org.abimon.visi.lang.times
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

object ScriptTextFormat : SpiralFormat {
    override val name = "Scripting"
    override val extension = "osl" //OP Scripting Language
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat, WRDFormat)

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
        return dataSource().use { stream ->
            val reader = BufferedReader(InputStreamReader(stream))

            val opNames: List<String> = ArrayList<String>().apply {
                addAll(SpiralData.dr1OpCodes.values.map { (_, name) -> name.toLowerCase() })
                addAll(SpiralData.dr2OpCodes.values.map { (_, name) -> name.toLowerCase() })
                addAll(SpiralData.drv3OpCodes.values.map { (_, name) -> name.toLowerCase() })
            }

            var foundOps = false
            reader.forEachLine { line ->
                if(foundOps)
                    return@forEachLine

                val parts = line.split("|", limit = 2)

                val opCode = parts[0].toLowerCase()

                if (opCode.startsWith("0x"))
                    foundOps = true
                else if (opCode.matches("\\d+".toRegex()))
                    foundOps = true
                else if (opCode in opNames)
                    foundOps = true
            }

            return@use foundOps
        }
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, dataSource, output, params)) return true

        if (game == V3 && format == LINFormat)
            throw IllegalArgumentException("Cannot convert to a V3 LIN File (No such thing; maybe you meant WRD?)")
        if (game != V3 && format == WRDFormat)
            throw IllegalArgumentException("Cannot convert to a non V3 WRD File (No such thing; maybe you meant LIN?)")

        debug("Begun Converting\n${"-" * 100}")
        when (format) {
            LINFormat -> {
                val dr1 = "${params["lin:dr1"] ?: true}".toBoolean()
                val ops: TripleHashMap<Int, Int, String> = if(dr1) SpiralData.dr1OpCodes else SpiralData.dr2OpCodes

                dataSource().use { stream ->
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
            WRDFormat -> {
                dataSource().use { stream ->
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