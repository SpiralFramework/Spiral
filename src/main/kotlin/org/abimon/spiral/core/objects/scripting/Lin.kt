package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.abimon.spiral.core.utils.*
import java.io.InputStream

class Lin(val game: HopesPeakDRGame, val dataSource: () -> InputStream) {
    val linType: Int
    val headerSpace: Int

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

            var index = 0
            val maximumRead = textBlock - headerSpace

            val linStream = LinkedListStream(stream)

            while (index < maximumRead) {
                var byte = linStream.read()
                index++

                while (byte != 0x70 && index < maximumRead) {
                    index++
                    byte = linStream.read()
                }

                if (index >= maximumRead)
                    break

                val opCode = linStream.read()
                val (_, argumentCount, getEntry) = game.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)
                val arguments: IntArray

                if (argumentCount == -1) {
                    val args: MutableList<Int> = ArrayList()

                    while (index < maximumRead && linStream.peek() != 0x70) {
                        index++
                        args.add(linStream.read())
                    }

                    arguments = args.toIntArray()
                } else {
                    arguments = IntArray(argumentCount) { linStream.read() }
                    index += arguments.size
                }

                if (arguments.size == argumentCount || argumentCount == -1) {
                    entries.add(getEntry(opCode, arguments))
                } else {
                    println("Wrong number of arguments for OP Code 0x${opCode.toString(16)}; expected $argumentCount and got ${arguments.size}")
                }

                index++
            }

            if (stream.streamOffset < textBlock) {
                val skipping = textBlock - stream.streamOffset
                println("${stream.streamOffset} is where we are, and we need to be at $textBlock; skipping $skipping bytes")

                stream.skip(skipping)
            } else if (stream.streamOffset > textBlock) {
                throw IllegalStateException("${stream.streamOffset} is where we are, and we were meant to stop at $textBlock; what happened???")
            }

            assertAsArgument(stream.streamOffset == textBlock.toLong(), "Illegal stream offset in Lin File (Was ${stream.streamOffset}, expected to be at $textBlock)")

            val textLines = stream.readInt32LE()
            val textPositions = IntArray(textLines + 1) { stream.readInt32LE() }
            val textEntries = entries.filterIsInstance(TextEntry::class.java)

            for (textID in 0 until textLines) {
                val line = ByteArray(textPositions[textID + 1] - textPositions[textID] - 2)
                stream.read(line)
                stream.readInt16LE() //Strings *are* zero terminated
                textEntries.firstOrNull { textEntry -> textEntry.textID == textID }?.text = String(line, Charsets.UTF_16)
            }

            this.entries = entries.toTypedArray()
        } finally {
            stream.close()
        }
    }
}