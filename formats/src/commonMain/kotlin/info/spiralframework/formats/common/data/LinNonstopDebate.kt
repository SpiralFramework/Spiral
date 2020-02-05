package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.readExact
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.use
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
class LinNonstopDebate(val baseTimeLimit: Int, val sections: Array<LinNonstopDebateSection>) {
    companion object {
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): LinNonstopDebate? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.hpa_nonstop_debate.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): LinNonstopDebate {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.hpa_nonstop_debate.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val timeLimit = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val sectionCount = requireNotNull(flow.readInt16LE(), notEnoughData)

                    val sectionBuffer = ByteArray(60)

                    val sections = Array(sectionCount) {
                        requireNotNull(flow.readExact(sectionBuffer), notEnoughData)
                        LinNonstopDebateSection.fromData(sectionBuffer)
                    }

                    return LinNonstopDebate(timeLimit, sections)
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
suspend fun SpiralContext.UnsafeHopesPeakNonstopDebate(dataSource: DataSource<*>) = LinNonstopDebate.unsafe(this, dataSource)