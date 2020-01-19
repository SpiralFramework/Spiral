package info.spiralframework.formats.common.scripting

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.fauxSeekFromStart
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.withFormats

@ExperimentalUnsignedTypes
class WordScript {
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

                    println()

                    return WordScript()
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
            if (length >= 0x80)
                length += (requireNotNull(flow.read(), notEnoughData) - 1) shl 8
            flow.readDoubleByteNullTerminatedString(length + 2, TextCharsets.UTF_16LE)
        }
    }
}