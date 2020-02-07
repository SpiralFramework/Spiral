package info.spiralframework.formats.common.scripting.wrd

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readDoubleByteNullTerminatedString
import info.spiralframework.base.common.io.readSingleByteNullTerminatedString
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.*

@ExperimentalUnsignedTypes
class WordScript(val labels: Array<String>, val parameters: Array<String>, val strings: Array<String>?, val localBranchNumbers: Array<Pair<Int, Int>>, val scriptDataBlocks: Array<Array<WrdEntry>>) {
    companion object {
        const val MAGIC_NUMBER_LE = 0x2E575244

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, game: DrGame.WordScriptable, dataSource: DataSource<*>): WordScript? {
            try {
                return unsafe(context, game, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.wrd.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, game: DrGame.WordScriptable, dataSource: DataSource<*>): WordScript {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.wrd.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val possibleMagicNumber = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val stringCount = if (possibleMagicNumber == MAGIC_NUMBER_LE) requireNotNull(flow.readInt16LE(), notEnoughData) else (possibleMagicNumber and 0xFFFF)
                    val labelCount = if (possibleMagicNumber == MAGIC_NUMBER_LE) requireNotNull(flow.readInt16LE(), notEnoughData) else ((possibleMagicNumber shr 16) and 0xFFFF)
                    val parameterCount = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val localBranchCount = requireNotNull(flow.readInt16LE(), notEnoughData)

                    val padding = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val localBranchOffset = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val sectionOffset = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val labelOffset = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val parameterOffset = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val stringOffset = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val labels = requireNotNull(flow.fauxSeekFromStart(labelOffset.toULong(), dataSource) { labelFlow ->
                        readParameterStrings(labelFlow, labelCount, notEnoughData)
                    })

                    val parameters = requireNotNull(flow.fauxSeekFromStart(parameterOffset.toULong(), dataSource) { parameterFlow ->
                        readParameterStrings(parameterFlow, parameterCount, notEnoughData)
                    })

                    val strings = if (stringOffset > 0) requireNotNull(flow.fauxSeekFromStart(stringOffset.toULong(), dataSource) { stringFlow ->
                        readStrings(stringFlow, stringCount, notEnoughData)
                    }) else null

                    val localBranchNumbers = requireNotNull(flow.fauxSeekFromStart(localBranchOffset.toULong(), dataSource) { localBranchFlow ->
                        Array(localBranchCount) {
                            val first = requireNotNull(flow.readInt16LE(), notEnoughData)
                            val second = requireNotNull(flow.readInt16LE(), notEnoughData)
                            Pair(first, second)
                        }
                    })

                    val sectionOffsets = requireNotNull(flow.fauxSeekFromStart(sectionOffset.toULong(), dataSource) { sectionFlow ->
                        IntArray(labelCount) { requireNotNull(sectionFlow.readInt16LE(), notEnoughData) }
                    })

                    val scriptDataBlocks = Array(labelCount) { index ->
                        val size: Int

                        if (index == labelCount - 1)
                            size = localBranchOffset - sectionOffsets[index]
                        else
                            size = sectionOffsets[index + 1] - sectionOffsets[index]

                        require(size >= 0) { localise("formats.wrd.bad_size", index, size) }
                        require(sectionOffsets[index] > 0) { localise("formats.wrd.bad_offset", index, sectionOffsets[index]) }

                        requireNotNull(flow.fauxSeekFromStart(sectionOffsets[index].toULong(), dataSource) { scriptDataFlow ->
                            readScriptData(labels, parameters, strings, game, BufferedInputFlow(WindowedInputFlow(scriptDataFlow, 0uL, size.toULong())))
                        })
                    }

                    return WordScript(labels, parameters, strings, localBranchNumbers, scriptDataBlocks)
                }
            }
        }

        @ExperimentalStdlibApi
        suspend fun SpiralContext.readParameterStrings(flow: InputFlow, count: Int, notEnoughData: () -> Any): Array<String> = Array(count) {
            val length = requireNotNull(flow.read(), notEnoughData)
            flow.readSingleByteNullTerminatedString(length + 1, TextCharsets.UTF_8)
        }

        @ExperimentalStdlibApi
        suspend fun SpiralContext.readStrings(flow: InputFlow, count: Int, notEnoughData: () -> Any): Array<String> = Array(count) {
            var length = requireNotNull(flow.read(), notEnoughData)
            if (length > 0x7F) {
                /**
                 *  Bitwise maths...
                 *  :dearlord:
                 *  The dark arts.
                 *      - Jill
                 */
                length = (length and 0x7F) or (requireNotNull(flow.read(), notEnoughData) shl 7)
            }
            flow.readDoubleByteNullTerminatedString(length + 2, TextCharsets.UTF_16LE)
        }

        suspend fun SpiralContext.readScriptData(labels: Array<String>, parameters: Array<String>, text: Array<String>?, game: DrGame.WordScriptable, flow: PeekableInputFlow): Array<WrdEntry> {
            withFormats(this) {
                val notEnoughData: () -> Any = { localise("formats.wrd.not_enough_data") }
                val entries: MutableList<WrdEntry> = ArrayList()

                while (true) {
                    val opStart = flow.peekInt16BE() ?: break

                    if (opStart and 0xFF00 != 0x7000)
                        break

                    flow.skip(2u)

                    val opcode = game.wrdOpcodeMap[opStart and 0x00FF]
                    val commandTypes = game.wrdOpcodeCommandType[opStart and 0x00FF]
//                    val arguments: IntArray

                    if (opcode?.flagCheckDetails != null) {
                        val flagCheckDetails = opcode.flagCheckDetails
                        val endFlagCheck = 0x7000 or flagCheckDetails.endFlagCheckOpcode
                        val flagGroup = ByteArray(flagCheckDetails.flagGroupLength * 2)
                        val rawArguments: MutableList<Int> = ArrayList()

                        requireNotNull(flow.readExact(flagGroup), notEnoughData)
                        for (i in 0 until flagCheckDetails.flagGroupLength) rawArguments.add(((flagGroup[i * 2].toInt() and 0xFF shl 8) or (flagGroup[i * 2 + 1].toInt() and 0xFF)))

                        while (true) {
                            if ((flow.peekInt16BE() ?: break) == endFlagCheck)
                                break

                            rawArguments.add(requireNotNull(flow.readInt16BE(), notEnoughData))

                            requireNotNull(flow.readExact(flagGroup), notEnoughData)
                            for (i in 0 until flagCheckDetails.flagGroupLength) rawArguments.add(((flagGroup[i * 2].toInt() and 0xFF shl 8) or (flagGroup[i * 2 + 1].toInt() and 0xFF)))
                        }

//                        arguments = rawArguments.toIntArray()

                        entries.add(opcode.entryConstructor(opcode.opcode, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    } else if (opcode?.argumentCount == -1) {
                        val rawArguments: MutableList<Int> = ArrayList()
                        while (true) {
                            if ((flow.peekInt16BE() ?: break) and 0xFF00 == 0x7000)
                                break

                            rawArguments.add(requireNotNull(flow.readInt16BE(), notEnoughData))
                        }
//                        arguments = rawArguments.toIntArray()

                        entries.add(opcode.entryConstructor(opcode.opcode, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    } else if (opcode != null) {
                        val rawArguments = ByteArray(opcode.argumentCount * 2)
                        requireNotNull(flow.readExact(rawArguments), notEnoughData)
//                        arguments = IntArray(opcode.argumentCount) { index -> ((rawArguments[index * 2].toInt() and 0xFF shl 8) or (rawArguments[index * 2 + 1].toInt() and 0xFF)) }

                        entries.add(opcode.entryConstructor(opcode.opcode, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    } else {
                        val rawArguments: MutableList<Int> = ArrayList()
                        while (true) {
                            if ((flow.peekInt16BE() ?: break) and 0xFF00 == 0x7000)
                                break

                            rawArguments.add(requireNotNull(flow.readInt16BE(), notEnoughData))
                        }
//                        arguments = rawArguments.toIntArray()

                        entries.add(UnknownWrdEntry(opStart and 0x00FF, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    }
                }

                return entries.toTypedArray()
            }
        }
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.WordScript(game: DrGame.WordScriptable, dataSource: DataSource<*>) = WordScript(this, game, dataSource)
@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeWordScript(game: DrGame.WordScriptable, dataSource: DataSource<*>) = WordScript.unsafe(this, game, dataSource)