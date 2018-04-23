package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.LinTextScript
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.abimon.spiral.core.utils.*
import java.io.InputStream
import java.util.*

class Lin private constructor(val game: HopesPeakDRGame, val dataSource: () -> InputStream) {
    companion object {
        val BOM_BE = 0xFEFF
        val BOM_LE = 0xFFFE
        val NULL_TERMINATOR = 0x00.toChar()

        operator fun invoke(game: HopesPeakDRGame, dataSource: () -> InputStream): Lin? {
            try {
                return Lin(game, dataSource)
            } catch (iae: IllegalArgumentException) {
                iae.printStackTrace(DataMapper.errorPrintStream)

                return null
            }
        }
    }

    val linType: Int
    val headerSpace: Int

    val textLines: Int

    val size: Int
    val textBlock: Int

    val header: ByteArray
    val entries: Array<LinScript>

    init {
        val stream = CountingInputStream(dataSource())

        try {
            linType = stream.readInt32LE()
            headerSpace = stream.readInt32LE()
            var headerSize = headerSpace - 8

            when (linType) {
                1 -> {
                    textBlock = stream.readInt32LE()
                    size = textBlock

                    headerSize -= 4
                }
                2 -> {
                    textBlock = stream.readInt32LE()
                    size = stream.readInt32LE()

                    headerSize -= 8
                }
                else -> throw IllegalArgumentException("Unknown Lin type $linType")
            }

            header = ByteArray(headerSize)
            val entries: MutableList<LinScript> = ArrayList()

            assertAsArgument(textBlock > headerSpace, "Invalid Lin File (expected textBlock to be greater than headerSpace, got $textBlock ≤ $headerSpace)")

            val maximumRead = textBlock - headerSpace

            val linData = LinkedList<Int>(ByteArray(maximumRead).apply { stream.read(this) }.map { byte -> byte.toInt() and 0xFF })

            while (linData.isNotEmpty()) {
                var byte = linData.poll() ?: break

                while (byte != 0x70 && linData.isNotEmpty())
                    byte = linData.poll() ?: break

                if (linData.isEmpty())
                    break

                val opCode = linData.poll() ?: break

                val (_, argumentCount, getEntry) = game.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)
                val arguments: IntArray

                val argumentRetrieval = game.customOpCodeArgumentReader[opCode]

                if (argumentRetrieval != null)
                    arguments = argumentRetrieval(linData)
                else if (argumentCount == -1) {
                    val args: MutableList<Int> = ArrayList()

                    while (linData.peek() != 0x70 && linData.isNotEmpty()) {
                        args.add(linData.poll() ?: break)
                    }

                    arguments = args.toIntArray()
                } else {
                    arguments = IntArray(argumentCount) { linData.poll() }
                }

                if (arguments.size == argumentCount || argumentCount == -1) {
                    entries.add(getEntry(opCode, arguments))
                } else {
                    println("Wrong number of arguments for OP Code 0x${opCode.toString(16)}; expected $argumentCount and got ${arguments.size}")
                }
            }

            if (stream.streamOffset < textBlock) {
                val skipping = textBlock - stream.streamOffset
                println("${stream.streamOffset} is where we are, and we need to be at $textBlock; skipping $skipping bytes")

                stream.skip(skipping)
            } else if (stream.streamOffset > textBlock) {
                throw IllegalStateException("${stream.streamOffset} is where we are, and we were meant to stop at $textBlock; what happened???")
            }

            assertAsArgument(stream.streamOffset == textBlock.toLong(), "Illegal stream offset in Lin File (Was ${stream.streamOffset}, expected to be at $textBlock)")

            val textLineCount = stream.readInt32LE()
            if (textBlock == size || textLineCount == -1) {
                textLines = 0
            } else {
                textLines = textLineCount
                val textPositions = IntArray(textLines + 1) { stream.readInt32LE() }
                if (textPositions[textLines] == 0)
                    textPositions[textLines] = this.size - this.textBlock

                val textEntries = entries.filterIsInstance(LinTextScript::class.java)

                for (textID in 0 until textLines) {
                    val size = textPositions[textID + 1] - textPositions[textID] - 2
                    if (size <= 0) {
                        //System.err.println("ERR: Lin file has text ID $textID as size $size; bad Lin file?")
                        continue
                    }

                    val line = ByteArray(size)
                    stream.read(line)
                    stream.readInt16LE() //Strings *are* zero terminated
                    val textEntry = textEntries.firstOrNull { textEntry -> textEntry.textID == textID }
                    val bom = if (line.size < 2) 0 else (((line[0].toInt() and 0xFF) shl 8) or (line[1].toInt() and 0xFF))

                    when (bom) {
                        BOM_BE -> textEntry?.text = String(line, Charsets.UTF_16).trimEnd(NULL_TERMINATOR)
                        BOM_LE -> textEntry?.text = String(line, Charsets.UTF_16).trimEnd(NULL_TERMINATOR)
                        else -> textEntry?.text = String(line, Charsets.UTF_16LE).trimEnd(NULL_TERMINATOR) //May need to do a when clause later
                    }
                }
            }

            assertAsArgument(entries.isNotEmpty(), "Illegal entry array in Lin File (entries is empty, must have at least one entry)")
            this.entries = entries.toTypedArray()
        } finally {
            stream.close()
        }
    }
}