package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.core.utils.*
import java.io.InputStream
import java.util.*

class WordScriptFile(val game: V3, val dataSource: () -> InputStream) {
    val entries: Array<Array<WrdScript>>
    val labels: Array<String>
    val parameters: Array<String>

    val strings: Array<String>

    init {
        val stream = CountingInputStream(dataSource())
        try {
            val stringCount = stream.readInt16LE()
            val labelCount = stream.readInt16LE()
            val parameterCount = stream.readInt16LE()
            val unkCount = stream.readInt16LE()

            val padding = stream.readInt32LE()
            val unkOffset = stream.readInt32LE()

            val sectionOffset = stream.readInt32LE()
            val labelOffset = stream.readInt32LE()
            val parameterOffset = stream.readInt32LE()
            val stringOffset = stream.readInt32LE()

            labels = dataSource().use { commandStream ->
                commandStream.skip(labelOffset.toLong())

                return@use Array(labelCount) {
                    val length = commandStream.read()

                    return@Array commandStream.readNullTerminatedString(length + 1, Charsets.UTF_8)
                }
            }

            parameters = dataSource().use { commandStream ->
                commandStream.skip(parameterOffset.toLong())

                return@use Array(parameterCount) {
                    val length = commandStream.read()

                    return@Array commandStream.readNullTerminatedString(length + 1, Charsets.UTF_8)
                }
            }

            strings = dataSource().use { stringStream ->
                stringStream.skip(stringOffset.toLong())
                return@use Array(stringCount) {
                    var stringLen = stream.read()

                    if(stringLen >= 0x80)
                        stringLen += (stream.read() - 1) shl 8

                    return@Array stream.readNullTerminatedString(stringLen + 2, Charsets.UTF_16LE, 2)
                }
            }

            val sectionOffsets = dataSource().use { commandStream ->
                commandStream.skip(sectionOffset.toLong())

                return@use IntArray(labelCount) { commandStream.readInt16LE() + 0x20 }
            }

            entries = Array(labelCount) { index ->
                val size: Int

                if (index == labelCount - 1)
                    size = unkOffset - sectionOffsets[index]
                else
                    size = sectionOffsets[index + 1] - sectionOffsets[index]

                return@Array dataSource().use { wrdStream ->
                    wrdStream.skip(sectionOffsets[index].toLong())
                    val wrdData = LinkedList<Int>(ByteArray(size).apply { wrdStream.read(this) }.map { byte -> byte.toInt() and 0xFF })
                    val wrdEntries: MutableList<WrdScript> = ArrayList()

                    while (wrdData.isNotEmpty()) {
                        var byte = wrdData.poll() ?: break

                        while (byte != 0x70 && wrdData.isNotEmpty())
                            byte = wrdData.poll() ?: break

                        if (wrdData.isEmpty())
                            break

                        val opCode = wrdData.poll() ?: break

                        val (_, argumentCount, getEntry) = game.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)
                        val arguments: IntArray

                        if (argumentCount == -1) {
                            val args: MutableList<Int> = ArrayList()

                            while (wrdData.peek() != 0x70 && wrdData.isNotEmpty()) {
                                args.add(wrdData.poll() ?: break)
                            }

                            arguments = args.toIntArray()
                        } else {
                            arguments = IntArray(argumentCount) { wrdData.poll() }
                        }

                        if (arguments.size == argumentCount || argumentCount == -1) {
                            wrdEntries.add(getEntry(opCode, arguments))
                        } else {
                            println("Wrong number of arguments for OP Code 0x${opCode.toString(16)}; expected $argumentCount and got ${arguments.size}")
                        }
                    }

                    return@use wrdEntries.toTypedArray()
                }
            }
        } finally {
            stream.close()
        }
    }

    operator fun get(command: EnumWordScriptCommand, index: Int): String {
        return when(command) {
            EnumWordScriptCommand.LABEL -> labels[index]
            EnumWordScriptCommand.PARAMETER -> parameters[index]
            EnumWordScriptCommand.STRING -> strings[index]
            else -> index.toString()
        }
    }
}