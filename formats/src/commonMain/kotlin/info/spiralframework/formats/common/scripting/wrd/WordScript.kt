package info.spiralframework.formats.common.scripting.wrd

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.consumeAndGetOrBreak
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.TextCharsets
import dev.brella.kornea.io.common.fauxSeekFromStartForResult
import dev.brella.kornea.io.common.flow.BufferedInputFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.PeekableInputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.peekInt16BE
import dev.brella.kornea.io.common.flow.extensions.readDoubleByteNullTerminatedString
import dev.brella.kornea.io.common.flow.extensions.readInt16BE
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readSingleByteNullTerminatedString
import dev.brella.kornea.io.common.flow.readExact
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
class WordScript(val labels: Array<String>, val parameters: Array<String>, val strings: Array<String>?, val localBranchNumbers: Array<Pair<Int, Int>>, val scriptDataBlocks: Array<Array<WrdEntry>>) {
    companion object {
        const val MAGIC_NUMBER_LE = 0x2E575244

        const val NOT_ENOUGH_DATA_KEY = "formats.wrd.not_enough_data"

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, game: DrGame.WordScriptable, dataSource: DataSource<*>): KorneaResult<WordScript> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .consumeAndGetOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val possibleMagicNumber = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val stringCount =
                            if (possibleMagicNumber == MAGIC_NUMBER_LE)
                                flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            else
                                possibleMagicNumber and 0xFFFF
                    val labelCount =
                            if (possibleMagicNumber == MAGIC_NUMBER_LE)
                                flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            else
                                (possibleMagicNumber shr 16) and 0xFFFF
                    val parameterCount = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val localBranchCount = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val padding = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val localBranchOffset = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val sectionOffset = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val labelOffset = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val parameterOffset = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val stringOffset = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val labels = flow.fauxSeekFromStartForResult(labelOffset.toULong(), dataSource) { labelFlow ->
                        readParameterStrings(labelFlow, labelCount)
                    }.getOrBreak { return@closeAfter it.cast() }

                    val parameters = flow.fauxSeekFromStartForResult(parameterOffset.toULong(), dataSource) { parameterFlow ->
                        readParameterStrings(parameterFlow, parameterCount)
                    }.getOrBreak { return@closeAfter it.cast() }

                    val strings = when {
                        stringOffset > 0 -> flow.fauxSeekFromStartForResult(stringOffset.toULong(), dataSource) { stringFlow ->
                            readStrings(stringFlow, stringCount)
                        }.getOrBreak { return@closeAfter it.cast() }
                        else -> null
                    }

                    val localBranchNumbers = flow.fauxSeekFromStartForResult(localBranchOffset.toULong(), dataSource) { localBranchFlow ->
                        KorneaResult.success(Array(localBranchCount) {
                            val first = flow.readInt16LE() ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val second = flow.readInt16LE() ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            Pair(first, second)
                        })
                    }.getOrBreak { return@closeAfter it.cast() }

                    val sectionOffsets = flow.fauxSeekFromStartForResult(sectionOffset.toULong(), dataSource) { sectionFlow ->
                        KorneaResult.success(IntArray(labelCount) {
                            sectionFlow.readInt16LE()?.plus(0x20) ?: return@fauxSeekFromStartForResult localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        })
                    }.getOrBreak { return@closeAfter it.cast() }

                    val scriptDataBlocks = Array(labelCount) { index ->
                        val size: Int

                        if (index == labelCount - 1)
                            size = localBranchOffset - sectionOffsets[index]
                        else
                            size = sectionOffsets[index + 1] - sectionOffsets[index]

                        require(size >= 0) { localise("formats.wrd.bad_size", index, size) }
                        require(sectionOffsets[index] > 0) { localise("formats.wrd.bad_offset", index, sectionOffsets[index]) }

                        flow.fauxSeekFromStartForResult(sectionOffsets[index].toULong(), dataSource) { scriptDataFlow ->
                            readScriptData(labels, parameters, strings, game, BufferedInputFlow(WindowedInputFlow(scriptDataFlow, 0uL, size.toULong())))
                        }.getOrBreak { return@closeAfter it.cast() }
                    }

                    return@closeAfter KorneaResult.success(WordScript(labels, parameters, strings, localBranchNumbers, scriptDataBlocks))
                }
            }

        @ExperimentalStdlibApi
        suspend fun SpiralContext.readParameterStrings(flow: InputFlow, count: Int): KorneaResult<Array<String>> {
            return KorneaResult.success(Array(count) {
                val length = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                flow.readSingleByteNullTerminatedString(length + 1, TextCharsets.UTF_8)
            })
        }

        @ExperimentalStdlibApi
        suspend fun SpiralContext.readStrings(flow: InputFlow, count: Int): KorneaResult<Array<String>> {
            return KorneaResult.success(Array(count) {
                var length = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                if (length > 0x7F) {
                    /**
                     *  Bitwise maths...
                     *  :dearlord:
                     *  The dark arts.
                     *      - Jill
                     */
                    length = flow.read()?.shl(7)?.or(length and 0x7F)
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                }
                flow.readDoubleByteNullTerminatedString(length + 2, TextCharsets.UTF_16LE)
            })
        }

        suspend fun SpiralContext.readScriptData(labels: Array<String>, parameters: Array<String>, text: Array<String>?, game: DrGame.WordScriptable, flow: PeekableInputFlow): KorneaResult<Array<WrdEntry>> {
            withFormats(this) {
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

                        flow.readExact(flagGroup) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        for (i in 0 until flagCheckDetails.flagGroupLength) rawArguments.add(((flagGroup[i * 2].toInt() and 0xFF shl 8) or (flagGroup[i * 2 + 1].toInt() and 0xFF)))

                        while (true) {
                            if ((flow.peekInt16BE() ?: break) == endFlagCheck)
                                break

                            rawArguments.add(flow.readInt16BE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))

                            flow.readExact(flagGroup) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            for (i in 0 until flagCheckDetails.flagGroupLength) rawArguments.add(((flagGroup[i * 2].toInt() and 0xFF shl 8) or (flagGroup[i * 2 + 1].toInt() and 0xFF)))
                        }

//                        arguments = rawArguments.toIntArray()

                        entries.add(opcode.entryConstructor(opcode.opcode, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    } else if (opcode?.argumentCount == -1) {
                        val rawArguments: MutableList<Int> = ArrayList()
                        while (true) {
                            if ((flow.peekInt16BE() ?: break) and 0xFF00 == 0x7000)
                                break

                            rawArguments.add(flow.readInt16BE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
                        }
//                        arguments = rawArguments.toIntArray()

                        entries.add(opcode.entryConstructor(opcode.opcode, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    } else if (opcode != null) {
                        val rawArguments = ByteArray(opcode.argumentCount * 2)
                        flow.readExact(rawArguments) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
//                        arguments = IntArray(opcode.argumentCount) { index -> ((rawArguments[index * 2].toInt() and 0xFF shl 8) or (rawArguments[index * 2 + 1].toInt() and 0xFF)) }

                        entries.add(opcode.entryConstructor(opcode.opcode, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    } else {
                        val rawArguments: MutableList<Int> = ArrayList()
                        while (true) {
                            if ((flow.peekInt16BE() ?: break) and 0xFF00 == 0x7000)
                                break

                            rawArguments.add(flow.readInt16BE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
                        }
//                        arguments = rawArguments.toIntArray()

                        entries.add(UnknownWrdEntry(opStart and 0x00FF, WordScriptValue.parse(rawArguments, labels, parameters, text, commandTypes)))
                    }
                }

                return KorneaResult.success(entries.toTypedArray())
            }
        }
    }
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.WordScript(game: DrGame.WordScriptable, dataSource: DataSource<*>) = WordScript(this, game, dataSource)

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeWordScript(game: DrGame.WordScriptable, dataSource: DataSource<*>) = WordScript(this, game, dataSource).get()