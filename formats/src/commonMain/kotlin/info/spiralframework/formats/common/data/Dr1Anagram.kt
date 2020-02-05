package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.use

@ExperimentalUnsignedTypes
class Dr1Anagram(val timeLimit: Int, val damageTaken: Int, val correctAnswerIndex: Int, val incorrectAnswerIndex: Int, val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int, val unk5: Int, val unk6: Int, val gentleFilledLetters: BooleanArray, val kindFilledLetters: BooleanArray, val meanFilledLetters: BooleanArray) {
    companion object {
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): Dr1Anagram? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.dr1_anagram.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): Dr1Anagram {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.dr1_anagram.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val timeLimit = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val letters = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val damageTaken = requireNotNull(flow.readInt16LE(), notEnoughData) //Health? 2000
                    val correctAnswerIndex = requireNotNull(flow.readInt16LE(), notEnoughData) //22_Anagram.pak
                    val incorrectAnswerIndex = requireNotNull(flow.readInt16LE(), notEnoughData) //22_Anagram.pak

                    val unk1 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk2 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk3 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk4 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk5 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk6 = requireNotNull(flow.readInt16LE(), notEnoughData)

                    val gentleFilledLetters = BooleanArray(letters) { requireNotNull(flow.readInt16LE(), notEnoughData) == 1 }
                    val kindFilledLetters = BooleanArray(letters) { requireNotNull(flow.readInt16LE(), notEnoughData) == 1 }
                    val meanFilledLetters = BooleanArray(letters) { requireNotNull(flow.readInt16LE(), notEnoughData) == 1 }

                    return Dr1Anagram(timeLimit, damageTaken, correctAnswerIndex, incorrectAnswerIndex, unk1, unk2, unk3, unk4, unk5, unk6, gentleFilledLetters, kindFilledLetters, meanFilledLetters)
                }
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1Anagram(dataSource: DataSource<*>) = Dr1Anagram(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1Anagram(dataSource: DataSource<*>) = Dr1Anagram.unsafe(this, dataSource)