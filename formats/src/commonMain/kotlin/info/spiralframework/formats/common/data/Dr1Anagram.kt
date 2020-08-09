package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlowStateSelector
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.int
import dev.brella.kornea.io.common.flow.int16
import dev.brella.kornea.io.common.flow.mapWithState
import dev.brella.kornea.toolkit.common.closeAfter

@ExperimentalUnsignedTypes
class Dr1Anagram(val timeLimit: Int, val damageTaken: Int, val correctAnswerIndex: Int, val incorrectAnswerIndex: Int, val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int, val unk5: Int, val unk6: Int, val gentleFilledLetters: BooleanArray, val kindFilledLetters: BooleanArray, val meanFilledLetters: BooleanArray) {
    companion object {
        const val NOT_ENOUGH_DATA_KEY = "formats.dr1_anagram.not_enough_data"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<Dr1Anagram> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .mapWithState(InputFlowStateSelector::int16)
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val timeLimit = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val letters = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val damageTaken = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //Health? 2000
                    val correctAnswerIndex = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //22_Anagram.pak
                    val incorrectAnswerIndex = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //22_Anagram.pak

                    val unk1 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk2 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk3 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk4 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk5 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk6 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val gentleFilledLetters = BooleanArray(letters) { flow.readInt16LE()?.equals(1) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) }
                    val kindFilledLetters = BooleanArray(letters) { flow.readInt16LE()?.equals(1) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) }
                    val meanFilledLetters = BooleanArray(letters) { flow.readInt16LE()?.equals(1) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) }

                    return@closeAfter KorneaResult.success(Dr1Anagram(timeLimit, damageTaken, correctAnswerIndex, incorrectAnswerIndex, unk1, unk2, unk3, unk4, unk5, unk6, gentleFilledLetters, kindFilledLetters, meanFilledLetters))
                }
            }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1Anagram(dataSource: DataSource<*>) = Dr1Anagram(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1Anagram(dataSource: DataSource<*>) = Dr1Anagram(this, dataSource).get()