package info.spiralframework.formats.common.data

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrThrow
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.readExact
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import kotlin.math.roundToInt

public class LinNonstopDebate(
    public val baseTimeLimit: Int,
    public val sections: Array<LinNonstopDebateSection>
) {
    public companion object {
        public const val NOT_ENOUGH_DATA_KEY: String = "formats.nonstop_debate.lin.not_enough_data"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<LinNonstopDebate> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return it.cast() }

                closeAfter(flow) {
                    val timeLimit = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sectionCount =
                        flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val sectionBuffer = ByteArray(60)

                    val sections = Array(sectionCount) {
                        flow.readExact(sectionBuffer) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        LinNonstopDebateSection.fromData(sectionBuffer)
                    }

                    return@closeAfter KorneaResult.success(LinNonstopDebate(timeLimit, sections))
                }
            }
    }

    /** 2 * timeLimit */
    public val gentleTimeLimit: Int = baseTimeLimit * 2

    /** 1 * timeLimit */
    public val kindTimeLimit: Int = baseTimeLimit * 1

    /** 0.8 * timeLimit */
    public val meanTimeLimit: Int = (baseTimeLimit * 0.8).roundToInt()
}

@Suppress("FunctionName")
public suspend fun SpiralContext.HopesPeakNonstopDebate(dataSource: DataSource<*>): KorneaResult<LinNonstopDebate> =
    LinNonstopDebate(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeHopesPeakNonstopDebate(dataSource: DataSource<*>): LinNonstopDebate =
    LinNonstopDebate(this, dataSource).getOrThrow()