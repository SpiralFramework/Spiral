package info.spiralframework.formats.common.scripting

import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readDoubleByteNullTerminatedString
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.*
import org.abimon.kornea.io.common.peekInt16BE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

@ExperimentalUnsignedTypes
class LinScript(val scriptData: Array<LinEntry>, val textData: Array<String>) {
    companion object {
        const val MAGIC_NUMBER_LE = 0x2E4C494E

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, game: DrGame.LinScriptable, dataSource: DataSource<*>): LinScript? {
            try {
                return unsafe(context, game, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.lin.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, game: DrGame.LinScriptable, dataSource: DataSource<*>): LinScript {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.lin.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val possibleMagicNumber = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val linBlockCount = if (possibleMagicNumber == MAGIC_NUMBER_LE) requireNotNull(flow.readInt32LE(), notEnoughData) else possibleMagicNumber
                    require(linBlockCount in 1..2) { localise("formats.lin.wrong_block_count", linBlockCount) }

                    val linBlocks = IntArray(linBlockCount + 1) { requireNotNull(flow.readInt32LE(), notEnoughData) }
                    val scriptData = requireNotNull(flow.fauxSeekFromStart(linBlocks[0].toULong(), dataSource) { scriptDataFlow ->
                        readScriptData(this, game, BufferedInputFlow(WindowedInputFlow(scriptDataFlow, 0uL, (linBlocks[1] - linBlocks[0]).toULong())))
                    })

                    val textData = if (linBlockCount == 1) emptyArray() else requireNotNull(flow.fauxSeekFromStart(linBlocks[1].toULong(), dataSource) { textDataFlow ->
                        readTextData(this, textDataFlow, linBlocks[1])
                    })

                    return LinScript(scriptData, textData)
                }
            }
        }

        suspend fun readScriptData(context: SpiralContext, game: DrGame.LinScriptable, flow: PeekableInputFlow): Array<LinEntry> {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.lin.not_enough_data") }
                val entries: MutableList<LinEntry> = ArrayList()

                while (true) {
                    val opStart = flow.peekInt16BE() ?: break

                    if (opStart and 0xFF00 != 0x7000)
                        break

                    flow.skip(2u)
                    val opcode = game.linOpcodeMap[opStart and 0x00FF]
                    val arguments: IntArray

                    if (opcode?.flagCheckDetails != null) {
                        val flagCheckDetails = opcode.flagCheckDetails
                        val endFlagCheck = 0x7000 or flagCheckDetails.endFlagCheckOpcode
                        val flagGroup = ByteArray(flagCheckDetails.flagGroupLength)
                        val rawArguments: MutableList<Int> = ArrayList()
                        require(flow.read(flagGroup) == 4, notEnoughData)
                        flagGroup.forEach { rawArguments.add(it.toInt() and 0xFF) }

                        while (true) {
                            if ((flow.peekInt16BE() ?: break) == endFlagCheck)
                                break

                            rawArguments.add(requireNotNull(flow.read(), notEnoughData))
                            require(flow.read(flagGroup) == 4, notEnoughData)
                            flagGroup.forEach { rawArguments.add(it.toInt() and 0xFF) }
                        }

                        arguments = rawArguments.toIntArray()

                        entries.add(opcode.entryConstructor(opcode.opcode, arguments))
                    } else if (opcode?.argumentCount ?: -1 == -1) {
                        val rawArguments: MutableList<Int> = ArrayList()
                        while (true) {
                            if ((flow.peek() ?: break) == 0x70)
                                break

                            rawArguments.add(requireNotNull(flow.read(), notEnoughData))
                        }
                        arguments = rawArguments.toIntArray()

                        entries.add(UnknownLinEntry(opStart and 0x00FF, arguments))
                    } else {
                        val rawArguments = ByteArray(opcode!!.argumentCount)
                        require(flow.read(rawArguments) == rawArguments.size, notEnoughData)
                        arguments = IntArray(rawArguments.size) { rawArguments[it].toInt() and 0xFF }

                        entries.add(opcode.entryConstructor(opcode.opcode, arguments))
                    }
                }

                return entries.toTypedArray()
            }
        }

        @ExperimentalStdlibApi
        suspend fun readTextData(context: SpiralContext, flow: InputFlow, textOffset: Int): Array<String> {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.lin.not_enough_data") }

                val stringCount = requireNotNull(flow.readInt32LE(), notEnoughData)
                if (stringCount <= 0)
                    return emptyArray()

                val offsets = Array(stringCount) { it to (requireNotNull(flow.readInt32LE(), notEnoughData).toLong() + textOffset) }.sortedBy(Pair<Int, Long>::second)
                val strings = arrayOfNulls<String>(stringCount)

                if (offsets[0].second >= flow.position().toInt()) {
                    offsets.forEach { (i, offset) ->
                        flow.seek(offset, InputFlow.FROM_BEGINNING)
                        strings[i] = flow.readDoubleByteNullTerminatedString(encoding = TextCharsets.UTF_16)
                    }
                } else {
                    println(":/")
                }

                return strings.requireNoNulls()
            }
        }
    }

    operator fun get(textID: Int): String = textData[textID]
}