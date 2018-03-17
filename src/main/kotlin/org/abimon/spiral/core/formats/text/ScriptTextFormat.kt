package org.abimon.spiral.core.formats.text

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.scripting.LINFormat
import org.abimon.spiral.core.formats.scripting.WRDFormat
import org.abimon.spiral.core.objects.customLin
import org.abimon.spiral.core.objects.customWordScript
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.abimon.spiral.core.utils.and
import org.abimon.spiral.util.debug
import org.abimon.spiral.util.shortToIntPair
import org.abimon.visi.lang.times
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

object ScriptTextFormat : SpiralFormat {
    override val name = "Scripting"
    override val extension = "osl" //OP Scripting Language
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat, WRDFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
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

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true

        debug("Begun Converting\n${"-" * 100}")
        when (format) {
            LINFormat -> {
                if (game !is HopesPeakDRGame)
                    throw IllegalArgumentException("Cannot convert to a non-Hope's Peak LIN File (No such thing; maybe you meant WRD for V3, or an undocumented format?)")

                dataSource().use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val lin= customLin {
                        reader.forEachLine loop@ { line ->
                            val parts = line.split("|", limit = 2)

                            val opCode = parts[0]

                            val op: Int
                            op = when {
                                opCode.startsWith("0x") -> opCode.substring(2).toInt(16)
                                opCode.matches("\\d+".toRegex()) -> opCode.toInt()
                                game.opCodes.values.any { (names) -> name in names } -> game.opCodes.entries.first { (_, pair) -> opCode in pair.first }.key
                                else -> return@loop
                            }

                            val (names, argCount, constructor) = game.opCodes[op] ?: (emptyArray<String>() to -1 and ::UnknownEntry)

                            //TODO: Change this from a hardcoded value (somehow)
                            if (op == 0x02) { //Text
                                add(parts[1])
                            } else {
                                val args = if (parts.size == 1 || parts[1].isBlank()) IntArray(0) else parts[1].split(",").map(String::trim).map(String::toInt).toIntArray()
                                if(argCount == -1 || argCount == args.size)
                                    add(constructor(op, args))
                            }
                        }
                    }
                    lin.compile(output)
                }
            }
            WRDFormat -> {
                dataSource().use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val wrd = customWordScript {
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
                                    add(org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry(op, IntArray(0)))
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

                                    add(org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry(op, args.toIntArray()))
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