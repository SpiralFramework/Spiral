package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.flow.readExact
import info.spiralframework.base.common.io.readInt16LE
import info.spiralframework.base.common.io.use
import info.spiralframework.formats.common.withFormats
import kotlin.math.round

@ExperimentalUnsignedTypes
class V3NonstopDebate(val baseTimeLimit: Int, val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int, val sections: Array<V3NonstopDebateSection>) {
    companion object {
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): V3NonstopDebate? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.hpa_nonstop_debate.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): V3NonstopDebate {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.v3_nonstop_debate.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val timeLimit = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val sectionCount = requireNotNull(flow.readInt16LE(), notEnoughData)

                    val unk1 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk2 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk3 = requireNotNull(flow.readInt16LE(), notEnoughData)
                    val unk4 = requireNotNull(flow.readInt16LE(), notEnoughData)

                    val sectionBuffer = ByteArray(404) //Yes, this is the actual count

                    val sections = Array(sectionCount) {
                        println(flow.position())
                        requireNotNull(flow.readExact(sectionBuffer), notEnoughData)
                        V3NonstopDebateSection.fromData(sectionBuffer)
                    }

                    return V3NonstopDebate(timeLimit, unk1, unk2, unk3, unk4, sections)
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