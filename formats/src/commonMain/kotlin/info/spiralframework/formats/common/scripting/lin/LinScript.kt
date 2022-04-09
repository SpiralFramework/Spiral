package info.spiralframework.formats.common.scripting.lin

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.peekInt16BE
import dev.brella.kornea.io.common.flow.extensions.readDoubleByteNullTerminatedString
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.games.Dr1
import info.spiralframework.formats.common.games.Dr2
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.games.UDG
import info.spiralframework.formats.common.withFormats

public class LinScript(
    public val scriptData: Array<LinEntry>,
    public val textData: Array<String>,
    public val game: DrGame.LinScriptable? = null
) {
    public companion object {
        public const val MAGIC_NUMBER_MASK_LE: Int = 0xFFFFFF00.toInt()
        public const val MAGIC_NUMBER_LE: Int = 0x4C494E00
        public const val MAGIC_NUMBER_GAME_MASK: Int = 0x000000FF
        public const val MAGIC_NUMBER_DR1: Int = 0x31
        public const val MAGIC_NUMBER_DR2: Int = 0x32
        public const val MAGIC_NUMBER_UDG: Int = 0xAE

        public const val WRONG_BLOCK_COUNT: Int = 0x0000
        public const val INVALID_STRING_COUNT: Int = 0x0010
        public const val INVALID_STRING_OFFSET: Int = 0x0011

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.lin.not_enough_data"
        public const val WRONG_BLOCK_COUNT_KEY: String = "formats.lin.wrong_block_count"
        public const val MISMATCHING_GAME_KEY: String = "formats.lin.mismatching_game"
        public const val INVALID_STRING_COUNT_KEY: String = "formats.lin.invalid_string_count"
        public const val INVALID_STRING_OFFSET_KEY: String = "formats.lin.invalid_string_offset"

        public suspend operator fun invoke(
            context: SpiralContext,
            game: DrGame.LinScriptable?,
            dataSource: DataSource<*>
        ): KorneaResult<LinScript> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val possibleMagicNumber =
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val linBlockCount =
                        if (possibleMagicNumber and MAGIC_NUMBER_MASK_LE == MAGIC_NUMBER_LE)
                            flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        else
                            possibleMagicNumber

                    if (linBlockCount !in 1..2) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(
                            WRONG_BLOCK_COUNT,
                            localise(WRONG_BLOCK_COUNT_KEY, linBlockCount)
                        )
                    }

//                    val game: DrGame.LinScriptable? = game?.takeUnless { game -> game == DrGame.LinScriptable.Unknown } ?: when (possibleMagicNumber and MAGIC_NUMBER_GAME_MASK) {
//                        MAGIC_NUMBER_DR1 -> Dr1() ?: game
//                        MAGIC_NUMBER_DR2 -> Dr2() ?: game
//                        MAGIC_NUMBER_UDG -> UDG() ?: game
//                        else -> game
//                    }

                    when (possibleMagicNumber and MAGIC_NUMBER_GAME_MASK) {
                        MAGIC_NUMBER_DR1 -> if (game != null && game !is Dr1)
                            warn(MISMATCHING_GAME_KEY, (game as? DrGame)?.identifier ?: game, "Dr1")

                        MAGIC_NUMBER_DR2 -> if (game != null && game !is Dr2)
                            warn(MISMATCHING_GAME_KEY, (game as? DrGame)?.identifier ?: game, "Dr2")

                        MAGIC_NUMBER_UDG -> if (game != null && game !is UDG)
                            warn(MISMATCHING_GAME_KEY, (game as? DrGame)?.identifier ?: game, "UDG")
                    }

                    val game: DrGame.LinScriptable? =
                        when (game) {
                            null -> {
                                when (possibleMagicNumber and MAGIC_NUMBER_GAME_MASK) {
                                    MAGIC_NUMBER_DR1 -> Dr1().getOrNull()
                                    MAGIC_NUMBER_DR2 -> Dr2().getOrNull()
                                    MAGIC_NUMBER_UDG -> UDG().getOrNull()
                                    else -> null
                                }
                            }
                            DrGame.LinScriptable.Unknown -> {
                                when (possibleMagicNumber and MAGIC_NUMBER_GAME_MASK) {
                                    MAGIC_NUMBER_DR1 -> Dr1().getOrNull() ?: game
                                    MAGIC_NUMBER_DR2 -> Dr2().getOrNull() ?: game
                                    MAGIC_NUMBER_UDG -> UDG().getOrNull() ?: game
                                    else -> game
                                }
                            }
                            else -> game
                        }

                    requireNotNull(game) { localise("formats.lin.no_game_provided") }

                    val linBlocks = IntArray(linBlockCount + 1) {
                        flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    }

                    val scriptData =
                        flow.fauxSeekFromStartForResult(linBlocks[0].toULong(), dataSource) { scriptDataFlow ->
                            readScriptData(
                                this,
                                game,
                                BufferedInputFlow(
                                    WindowedInputFlow(
                                        scriptDataFlow,
                                        0uL,
                                        (linBlocks[1] - linBlocks[0]).toULong()
                                    )
                                )
                            )
                        }.getOrBreak { return@closeAfter it.cast() }

                    val textData = when (linBlockCount) {
                        1 -> emptyArray()
                        else -> {
                            val result = if (flow is SeekableInputFlow) {
                                bookmark(flow) {
                                    flow.seek(linBlocks[1].toLong(), EnumSeekMode.FROM_BEGINNING)
                                    readTextData(this, flow, linBlocks[1])
                                }
                            } else {
                                dataSource.openInputFlow().map { subflow ->
                                    if (subflow is SeekableInputFlow) subflow
                                    else BinaryInputFlow(subflow.readAndClose())
                                }.flatMap { subflow ->
                                    readTextData(this, subflow, linBlocks[1])
                                }
                            }

                            result.switchIfEmpty {
                                if (linBlocks.size > 2 && linBlocks[2] - linBlocks[1] <= 8) KorneaResult.success(
                                    emptyArray()
                                )
                                else KorneaResult.errorAsIllegalArgument(
                                    INVALID_STRING_COUNT,
                                    localise(INVALID_STRING_COUNT_KEY, 0)
                                )
                            }.getOrBreak { error -> return@closeAfter error.cast() }
                        }
                    }

                    return@closeAfter KorneaResult.success(LinScript(scriptData, textData, game))
                }
            }

        public suspend fun readScriptData(
            context: SpiralContext,
            game: DrGame.LinScriptable,
            flow: PeekableInputFlow
        ): KorneaResult<Array<LinEntry>> {
            withFormats(context) {
                val entries: MutableList<LinEntry> = ArrayList()

                while (true) {
                    val opStart = flow.peekInt16BE() ?: break

                    if (opStart and 0xFF00 != 0x7000)
                        break

                    flow.skip(2u)
                    val opcode = game.linOpcodeMap[opStart and 0x00FF]
//                    val arguments: IntArray

                    if (opcode?.flagCheckDetails != null) {
                        val flagCheckDetails = opcode.flagCheckDetails
                        val endFlagCheck = 0x7000 or flagCheckDetails.endFlagCheckOpcode
                        val flagGroup = ByteArray(flagCheckDetails.flagGroupLength)
                        val rawArguments: MutableList<Int> = ArrayList()
                        flow.readExact(flagGroup) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        flagGroup.forEach { rawArguments.add(it.toInt() and 0xFF) }

                        while (true) {
                            if ((flow.peekInt16BE() ?: break) == endFlagCheck)
                                break

                            rawArguments.add(flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
                            flow.readExact(flagGroup) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            flagGroup.forEach { rawArguments.add(it.toInt() and 0xFF) }
                        }

//                        arguments = rawArguments.toIntArray()

                        entries.add(opcode.entryConstructor(opcode.opcode, rawArguments.toIntArray()))
                    } else if (opcode?.argumentCount == -1) {
                        val rawArguments: MutableList<Int> = ArrayList()
                        while (true) {
                            if ((flow.peek() ?: break) == 0x70)
                                break

                            rawArguments.add(flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
                        }
//                        arguments = rawArguments.toIntArray()

                        entries.add(opcode.entryConstructor(opcode.opcode, rawArguments.toIntArray()))
                    } else if (opcode != null) {
                        val rawArguments = ByteArray(opcode.argumentCount)
                        flow.readExact(rawArguments) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
//                        arguments = IntArray(rawArguments.size) { rawArguments[it].toInt() and 0xFF }

                        entries.add(
                            opcode.entryConstructor(
                                opcode.opcode,
                                IntArray(rawArguments.size) { rawArguments[it].toInt() and 0xFF })
                        )
                    } else {
                        val rawArguments: MutableList<Int> = ArrayList()
                        while (true) {
                            if ((flow.peek() ?: break) == 0x70)
                                break

                            rawArguments.add(flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
                        }
//                        arguments = rawArguments.toIntArray()

                        entries.add(UnknownLinEntry(opStart and 0x00FF, rawArguments.toIntArray()))
                    }
                }

                return KorneaResult.success(entries.toTypedArray())
            }
        }

        public suspend fun readTextData(
            context: SpiralContext,
            flow: SeekableInputFlow,
            textOffset: Int
        ): KorneaResult<Array<String>> {
            withFormats(context) {
                val stringCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                if (stringCount == 0)
                    return KorneaResult.empty()
                else if (stringCount < 0)
                    return KorneaResult.errorAsIllegalArgument(
                        INVALID_STRING_COUNT,
                        localise(INVALID_STRING_COUNT_KEY, stringCount)
                    )

                val offsets = Array(stringCount) { index ->
                    Pair(
                        index, flow.readInt32LE()?.toLong()?.plus(textOffset)
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    )
                }.sortedBy(Pair<Int, Long>::second)

                val strings = arrayOfNulls<String>(stringCount)

                if (offsets[0].second >= flow.position().toInt()) {
                    offsets.forEach { (i, offset) ->
                        flow.seek(offset, EnumSeekMode.FROM_BEGINNING)
                        strings[i] = flow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16)
                    }
                } else {
                    return KorneaResult.errorAsIllegalArgument(
                        INVALID_STRING_OFFSET,
                        localise(
                            INVALID_STRING_OFFSET_KEY,
                            offsets[0].second.toHexString(),
                            flow.position().toLong().toHexString()
                        )
                    )
                }

                return KorneaResult.success(strings.requireNoNulls())
            }
        }
    }

    public operator fun get(textID: Int): String = textData[textID]
}

@Suppress("FunctionName")
public suspend fun SpiralContext.LinScript(game: DrGame.LinScriptable, dataSource: DataSource<*>): KorneaResult<LinScript> =
    LinScript(this, game, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeLinScript(game: DrGame.LinScriptable, dataSource: DataSource<*>): LinScript =
    LinScript(this, game, dataSource).getOrThrow()