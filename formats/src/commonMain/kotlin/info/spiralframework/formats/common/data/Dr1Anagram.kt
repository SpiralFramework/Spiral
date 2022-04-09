package info.spiralframework.formats.common.data

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrThrow
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats

public class Dr1Anagram(
    public val timeLimit: Int,
    public val damageTaken: Int,
    public val correctAnswerIndex: Int,
    public val incorrectAnswerIndex: Int,
    public val unk1: Int,
    public val unk2: Int,
    public val unk3: Int,
    public val unk4: Int,
    public val unk5: Int,
    public val unk6: Int,
    public val gentleFilledLetters: BooleanArray,
    public val kindFilledLetters: BooleanArray,
    public val meanFilledLetters: BooleanArray
) {
    public companion object {
        public const val NOT_ENOUGH_DATA_KEY: String = "formats.dr1_anagram.not_enough_data"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<Dr1Anagram> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val timeLimit = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val letters = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val damageTaken = flow.readInt16LE()
                        ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //Health? 2000
                    val correctAnswerIndex = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(
                        NOT_ENOUGH_DATA_KEY
                    ) //22_Anagram.pak
                    val incorrectAnswerIndex = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(
                        NOT_ENOUGH_DATA_KEY
                    ) //22_Anagram.pak

                    val unk1 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk2 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk3 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk4 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk5 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk6 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val gentleFilledLetters = BooleanArray(letters) {
                        flow.readInt16LE()?.equals(1) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    }
                    val kindFilledLetters = BooleanArray(letters) {
                        flow.readInt16LE()?.equals(1) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    }
                    val meanFilledLetters = BooleanArray(letters) {
                        flow.readInt16LE()?.equals(1) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    }

                    return@closeAfter KorneaResult.success(
                        Dr1Anagram(
                            timeLimit,
                            damageTaken,
                            correctAnswerIndex,
                            incorrectAnswerIndex,
                            unk1,
                            unk2,
                            unk3,
                            unk4,
                            unk5,
                            unk6,
                            gentleFilledLetters,
                            kindFilledLetters,
                            meanFilledLetters
                        )
                    )
                }
            }
    }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.Dr1Anagram(dataSource: DataSource<*>): KorneaResult<Dr1Anagram> = Dr1Anagram(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeDr1Anagram(dataSource: DataSource<*>): Dr1Anagram = Dr1Anagram(this, dataSource).getOrThrow()