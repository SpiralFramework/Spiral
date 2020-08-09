package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.toolkit.common.closeAfter
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
class LinNonstopDebate(val baseTimeLimit: Int, val sections: Array<LinNonstopDebateSection>) {
    companion object {
        const val NOT_ENOUGH_DATA_KEY = "formats.nonstop_debate.lin.not_enough_data"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<LinNonstopDebate> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .mapWithState(InputFlowStateSelector::int16)
                    .getOrBreak { return it.cast() }

                closeAfter(flow) {
                    val timeLimit = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sectionCount = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

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
    val gentleTimeLimit = baseTimeLimit * 2

    /** 1 * timeLimit */
    val kindTimeLimit = baseTimeLimit * 1

    /** 0.8 * timeLimit */
    val meanTimeLimit = (baseTimeLimit * 0.8).roundToInt()
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.HopesPeakNonstopDebate(dataSource: DataSource<*>) = LinNonstopDebate(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeHopesPeakNonstopDebate(dataSource: DataSource<*>) = LinNonstopDebate(this, dataSource).get()