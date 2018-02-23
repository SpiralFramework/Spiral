package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.core.utils.*
import java.io.InputStream

class WordScriptFile(val game: V3, val dataSource: () -> InputStream) {
    val entries: Array<WrdScript>
    val commandOneEntries: Array<String>
    val commandTwoEntries: Array<String>
    val commandThreeEntries: Array<String>

    init {
        val stream = CountingInputStream(dataSource())
        try {
            val stringCount = stream.readInt16LE()
            val cmd1Count = stream.readInt16LE()
            val cmd2Count = stream.readInt16LE()
            val cmd3Count = stream.readInt16LE()

            val unk = stream.readInt32LE()
            val unkOffset = stream.readInt32LE()

            val cmd3Offset = stream.readInt32LE()
            val cmd1Offset = stream.readInt32LE()
            val cmd2Offset = stream.readInt32LE()
            val stringOffset = stream.readInt32LE()

            val wrdStream = LinkedListStream(stream)
            val wrdEntries: MutableList<WrdScript> = ArrayList()
            val maxRead = unkOffset - 0x20

            var index = 0

            while (index < maxRead) {
                var byte = wrdStream.read()
                index++

                while (byte != 0x70 && index < maxRead) {
                    index++
                    byte = wrdStream.read()
                }

                if (index >= maxRead)
                    break

                val opCode = wrdStream.read()
                index++
                val (names, argumentCount, getEntry) = game.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)
                val arguments: IntArray

                if (argumentCount == -1) {
                    val args: MutableList<Int> = ArrayList()

                    while (index < maxRead && wrdStream.peek() != 0x70) {
                        index++
                        args.add(wrdStream.read())
                    }

                    arguments = args.toIntArray()
                } else {
                    arguments = IntArray(argumentCount) { wrdStream.read() }
                    index += argumentCount
                }

                if (arguments.size == argumentCount || argumentCount == -1)
                    wrdEntries.add(getEntry(opCode, arguments))
                else
                    println("Wrong number of arguments for OP Code 0x${opCode.toString(16)}; expected $argumentCount and got ${arguments.size}")
            }

            entries = wrdEntries.toTypedArray()

            val unkItems = IntArray(unk) { stream.read() }
            stream.skip((cmd3Offset - stream.count).coerceAtLeast(0))

            commandThreeEntries = Array(cmd3Count) {
                val length = stream.read()
                val str = stream.readString(length, "UTF-8")
                stream.read() //Null Terminated

                return@Array str
            }

            stream.skip((cmd1Offset - stream.count).coerceAtLeast(0))

            commandOneEntries = Array(cmd1Count) {
                val length = stream.read()
                val str = stream.readString(length, "UTF-8")
                stream.read() //Null Terminated

                return@Array str
            }

            stream.skip((cmd2Offset - stream.count).coerceAtLeast(0))

            commandTwoEntries = Array(cmd2Count) {
                val length = stream.read()
                val str = stream.readString(length, "UTF-8")
                stream.read() //Null Terminated

                return@Array str
            }
        } finally {
            stream.close()
        }
    }
}