package info.spiralframework.formats.scripting

import info.spiralframework.base.CountingInputStream
import info.spiralframework.base.util.locale
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readNullTerminatedString
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.scripting.wrd.UnknownEntry
import info.spiralframework.formats.scripting.wrd.WrdScript
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.utils.and
import java.io.InputStream
import java.util.*

class WordScriptFile private constructor(val game: V3, val dataSource: () -> InputStream) {
    companion object {
        operator fun invoke(game: V3, dataSource: () -> InputStream): WordScriptFile? {
            try {
                return WordScriptFile(game, dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.wrd.invalid", dataSource, game, iae)

                return null
            }
        }

        fun unsafe(game: V3, dataSource: DataSource): WordScriptFile = WordScriptFile(game, dataSource)
    }

    val entries: Array<Array<WrdScript>>
    val labels: Array<String>
    val parameters: Array<String>
    val localBranchNumbers: Array<Pair<Int, Int>>

    val strings: Array<String>
    val stringCount: Int

    init {
        val stream = CountingInputStream(dataSource())
        try {
            stringCount = stream.readInt16LE()
            val labelCount = stream.readInt16LE()
            val parameterCount = stream.readInt16LE()
            val localBranchCount = stream.readInt16LE()

            val padding = stream.readInt32LE()
            val localBranchOffset = stream.readInt32LE()

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

            if (stringOffset > 0)
                strings = dataSource().use { stringStream ->
                    stringStream.skip(stringOffset.toLong())
                    return@use Array(stringCount) {
                        var stringLen = stream.read()

                        if (stringLen >= 0x80)
                            stringLen += (stream.read() - 1) shl 8

                        return@Array stream.readNullTerminatedString(stringLen + 2, Charsets.UTF_16LE, 2)
                    }
                }
            else
                strings = emptyArray()

            localBranchNumbers = dataSource().use { stringStream ->
                stringStream.skip(localBranchOffset.toLong())
                return@use Array(localBranchCount) { stream.readInt16LE() to stream.readInt16LE() }
            }

            val sectionOffsets = dataSource().use { commandStream ->
                commandStream.skip(sectionOffset.toLong())

                return@use IntArray(labelCount) { commandStream.readInt16LE() + 0x20 }
            }

            entries = Array(labelCount) { index ->
                val size: Int

                if (index == labelCount - 1)
                    size = localBranchOffset - sectionOffsets[index]
                else
                    size = sectionOffsets[index + 1] - sectionOffsets[index]

                if (size < 0) {
                    throw locale<IllegalArgumentException>("formats.wrd.bad_size", index, size)
                } else if (sectionOffsets[index] <= 0) {
                    throw locale<IllegalArgumentException>("formats.wrd.bad_offset", index, sectionOffsets[index])
                }

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
                        val rawArguments: IntArray

                        //*Should* work-ish
                        if (argumentCount == -1) {
                            val args: MutableList<Int> = ArrayList()

                            while (wrdData.peek() != 0x70 && wrdData.isNotEmpty()) {
                                args.add(wrdData.poll() ?: break)
                            }

                            args.dropLast(args.size % 2)
                            rawArguments = args.toIntArray()
                        } else {
                            rawArguments = IntArray(argumentCount) { wrdData.poll() }
                        }

                        val arguments = IntArray(rawArguments.size / 2) { index -> ((rawArguments[index * 2] shl 8) or rawArguments[index * 2 + 1]) }

                        if (arguments.size != argumentCount / 2 && argumentCount != -1) {
                            DataHandler.LOGGER.warn("formats.wrd.wrong_arg_count", "0x${opCode.toString(16)}", argumentCount, arguments.size)
                        }

                        wrdEntries.add(getEntry(opCode, arguments))
                    }

                    return@use wrdEntries.toTypedArray()
                }
            }
        } finally {
            stream.close()
        }
    }

    operator fun get(command: EnumWordScriptCommand, index: Int): String {
        return when (command) {
            EnumWordScriptCommand.LABEL -> labels[index]
            EnumWordScriptCommand.PARAMETER -> parameters[index]
            EnumWordScriptCommand.STRING -> strings[index]
            else -> index.toString()
        }
    }
}