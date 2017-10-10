package org.abimon.spiral.core.objects

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.readShort
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.spiral.core.toIntArray
import org.abimon.spiral.core.wrd.SpeakerEntry
import org.abimon.spiral.core.wrd.TextEntry
import org.abimon.spiral.core.wrd.UnknownEntry
import org.abimon.spiral.core.wrd.WRDScript
import org.abimon.spiral.util.SeekableInputStream
import org.abimon.spiral.util.toShort
import org.abimon.spiral.util.trace
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readPartialBytes

class WRD(val dataSource: DataSource) {
    val entries: Array<WRDScript>
    val strings: Array<String>
    val cmds: Map<Int, Array<String>>

    init {
        val stream = SeekableInputStream(dataSource.seekableInputStream)
        try {
            val stringCount = stream.readShort(true, true)
            val cmd1Count = stream.readShort(true, true)
            val cmd2Count = stream.readShort(true, true)
            val cmd3Count = stream.readShort(true, true)

            val unk = stream.readUnsignedLittleInt()
            val unkOffset = stream.readUnsignedLittleInt()

            val cmd3Offset = stream.readUnsignedLittleInt()
            val cmd1Offset = stream.readUnsignedLittleInt()
            val cmd2Offset = stream.readUnsignedLittleInt()
            val stringOffset = stream.readUnsignedLittleInt()

            val code = stream.readPartialBytes((unkOffset - 0x20).toInt()).toIntArray()

            val cmdInfo = arrayOf(
                    (cmd1Count to cmd1Offset),
                    (cmd2Count to cmd2Offset),
                    (cmd3Count to cmd3Offset)
            )

            val commands: MutableMap<Int, Array<String>> = HashMap()

            for (cmdNum in cmdInfo.indices) {
                val (count, offset) = cmdInfo[cmdNum]
                val commandList: MutableList<String> = ArrayList()
                stream.seek(offset)

                for (i in 0 until count) {
                    val stringLength = stream.read()
                    val cmd = stream.readString(stringLength, "UTF-8")
                    stream.read()

                    commandList.add(cmd)
                }

                commands[cmdNum] = commandList.toTypedArray()
            }

            stream.seek(stringOffset)

            strings = Array<String>(stringCount, { "" })
            for(i in 0 until stringCount) {
                var stringLen = stream.read()

                if(stringLen >= 0x80)
                    stringLen += (stream.read() - 1) * 0x80

                val string = stream.readString(stringLen, "UTF-16LE")
                stream.readShort()

                strings[i] = string
            }

            var i = 0
            val wrdEntries: MutableList<WRDScript> = ArrayList()

            for (j in 0 until code.size) {
                if (i >= code.size)
                    break

                if (code[i] == 0x0) {
                    trace("$i is 0x0")
                    i++
                } else if (code[i] != 0x70) {
                    while (i < code.size) {
                        trace("$i expected to be 0x70, was ${code[i]}")
                        if (i == 0x00 || i == 0x70)
                            break
                        i++
                    }
                } else {
                    i++
                    val opCode = code[i++]
                    val (argumentCount) = SpiralData.drv3OpCodes.getOrDefault(opCode, Pair(-1, opCode.toString(16)))
                    val params: IntArray

                    if (argumentCount == -1) {
                        val args = ArrayList<Int>()
                        while (i < code.size && code[i] != 0x70) {
                            args.add(code[i++])
                        }
                        params = args.toIntArray()
                    } else {
                        params = IntArray(argumentCount)

                        for (argumentIndex in 0 until argumentCount) {
                            params[argumentIndex] = code[i++]
                        }
                    }

                    when(opCode) {
                        0x1D -> { ensure(0x1D, 2, params); wrdEntries.add(SpeakerEntry(toShort(params, true, false))) }
                        0x46 -> { ensure(0x46, 2, params); wrdEntries.add(TextEntry(toShort(params, true, false))) }
                        0x53 -> { ensure(0x53, 2, params); wrdEntries.add(SpeakerEntry(toShort(params, true, false))) }
                        0x58 -> { ensure(0x58, 2, params); wrdEntries.add(TextEntry(toShort(params, true, false))) }
                        else -> wrdEntries.add(UnknownEntry(opCode, params))
                    }
                }
            }

            entries = wrdEntries.toTypedArray()
            cmds = commands
        } finally {
            stream.close()
        }
    }

    private fun ensure(opCode: Int, required: Int, params: IntArray): Unit = if(params.size != required) throw IllegalArgumentException("Malformed WRD entry - 0x${opCode.toString(16)} (Expected $required, got ${params.size})") else Unit
}