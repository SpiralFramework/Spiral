package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.flow.readExact
import info.spiralframework.base.common.io.readInt16LE
import info.spiralframework.base.common.io.use
import info.spiralframework.formats.common.withFormats
import kotlin.math.round

@ExperimentalUnsignedTypes
class HopesPeakNonstopDebate(val baseTimeLimit: Int, val sections: Array<HopesPeakNonstopDebateSection>) {
    companion object {
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): HopesPeakNonstopDebate? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.hpa_nonstop_debate.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): HopesPeakNonstopDebate {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.hpa_nonstop_debate.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val timeLimit = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val sectionCount = requireNotNull(flow.readInt16LE(), notEnoughData)

                    val sectionBuffer = ByteArray(60)

                    val sections = Array(sectionCount) {
                        requireNotNull(flow.readExact(sectionBuffer), notEnoughData)
                        HopesPeakNonstopDebateSection.fromData(sectionBuffer)
                    }

                    return HopesPeakNonstopDebate(timeLimit, sections)
                }
            }
        }
    }

    /** 2 * timeLimit */
    val gentleTimeLimit = baseTimeLimit * 2

    /** 1 * timeLimit */
    val kindTimeLimit = baseTimeLimit * 1

    /** 0.8 * timeLimit */
    val meanTimeLimit = round(baseTimeLimit * 0.8).toInt()
}