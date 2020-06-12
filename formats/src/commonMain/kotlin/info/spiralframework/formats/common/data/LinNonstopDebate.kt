package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.erorrs.common.cast
import org.abimon.kornea.erorrs.common.doOnFailure
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.readExact
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.use
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
class LinNonstopDebate(val baseTimeLimit: Int, val sections: Array<LinNonstopDebateSection>) {
    companion object {
        const val NOT_ENOUGH_DATA_KEY = "formats.nonstop_debate.lin.not_enough_data"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<LinNonstopDebate> {
            withFormats(context) {
                val flow = dataSource.openInputFlow().doOnFailure { return it.cast() }

                use(flow) {
                    val timeLimit = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sectionCount = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val sectionBuffer = ByteArray(60)

                    val sections = Array(sectionCount) {
                        flow.readExact(sectionBuffer) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        LinNonstopDebateSection.fromData(sectionBuffer)
                    }

                    return KorneaResult.Success(LinNonstopDebate(timeLimit, sections))
                }
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