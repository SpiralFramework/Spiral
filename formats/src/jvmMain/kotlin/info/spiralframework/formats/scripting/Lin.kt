package info.spiralframework.formats.scripting

import info.spiralframework.base.jvm.io.CountingInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.common.games.hpa.HopesPeakDRGame
import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.formats.scripting.lin.LinTextScript
import info.spiralframework.formats.scripting.lin.UnknownEntry
import info.spiralframework.formats.utils.and
import java.io.InputStream
import java.util.*

class Lin private constructor(context: SpiralContext, val game: HopesPeakDRGame, val dataSource: () -> InputStream) {
    companion object {
        val BOM_BE = 0xFEFF
        val BOM_LE = 0xFFFE
        val NULL_TERMINATOR = 0x00.toChar()

        var MAXIMUM_LIN_READ = 1024 * 1024 * 8 // 8 MB maximum read
        var MAXIMUM_LIN_STRING = 1024 * 8 // 8 KB String

        operator fun invoke(context: SpiralContext, game: HopesPeakDRGame, dataSource: () -> InputStream): Lin? {
            withFormats(context) {
                try {
                    return Lin(this, game, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.lin.invalid", dataSource, game, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, game: HopesPeakDRGame, dataSource: () -> InputStream): Lin = withFormats(context) { Lin(this, game, dataSource) }
    }

    val linType: Int
    val headerSpace: Int

    val textLines: Int

    val size: Int
    val textBlock: Int

    val header: ByteArray
    val entries: Array<LinScript>

    init {
        with(context) {
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
                    else -> throw IllegalArgumentException(localise("formats.lin.invalid_type", linType))
                }

                header = ByteArray(headerSize)
                val entries: MutableList<LinScript> = ArrayList()

                require(textBlock > headerSpace) { context.localise("formats.lin.invalid_text_block", textBlock, headerSpace) }

                val maximumRead = textBlock - headerSpace

                require(maximumRead < MAXIMUM_LIN_READ) { context.localise("formats.lin.invalid_maximum_read", maximumRead, MAXIMUM_LIN_READ) }

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
                    else { //if (argumentCount == -1)
                        val args: MutableList<Int> = ArrayList()

                        while (linData.peek() != 0x70 && linData.isNotEmpty()) {
                            args.add(linData.poll() ?: break)
                        }

                        arguments = args.toIntArray()
                    }
//                else {
//                    arguments = IntArray(argumentCount) { linData.poll() }
//                }

                    if (arguments.size != argumentCount && argumentCount != -1) {
                        warn("formats.lin.wrong_arg_count", "0x${opCode.toString(16)}", argumentCount, arguments.size)
                    }

                    entries.add(getEntry(opCode, arguments))
                }

                if (stream.count < textBlock) {
                    val skipping = textBlock - stream.count
                    debug("formats.lin.undershot_block", stream.count, textBlock, skipping)

                    stream.skip(skipping)
                } else if (stream.count > textBlock) {
                    throw IllegalArgumentException(context.localise("formats.lin.overshot_text_block", stream.count, textBlock))
                }

                require(stream.count == textBlock.toLong()) { context.localise("formats.lin.not_at_text_block", stream.count, textBlock) }

                val textLineCount = stream.readInt32LE()
                if (textBlock == size || textLineCount == -1) {
                    textLines = 0
                } else {
                    textLines = textLineCount
                    val textPositions = IntArray(textLines + 1) { stream.readInt32LE() }
                    if (textPositions[textLines] == 0)
                        textPositions[textLines] = size - textBlock

                    val textEntries = entries.filterIsInstance(LinTextScript::class.java)

                    for (textID in 0 until textLines) {
                        val size = textPositions[textID + 1] - textPositions[textID] - 2
                        if (size <= 0) {
                            debug("formats.lin.text_size_zero", textID, size)
                            continue
                        }

                        require(size < MAXIMUM_LIN_STRING) { context.localise("formats.lin.text_size_too_large", size, MAXIMUM_LIN_STRING) }

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

                require(entries.isNotEmpty()) { context.localise("formats.lin.empty") }
                this@Lin.entries = entries.toTypedArray()
            } finally {
                stream.close()
            }
        }
    }
}