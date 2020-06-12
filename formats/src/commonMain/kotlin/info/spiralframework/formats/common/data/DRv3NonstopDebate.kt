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
import kotlin.math.round

@ExperimentalUnsignedTypes
class DRv3NonstopDebate(val baseTimeLimit: Int, val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int, val sections: Array<DRv3NonstopDebateSection>) {
    companion object {
        const val NOT_ENOUGH_DATA_KEY = "formats.nonstop_debate.drv3.not_enough_data"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<DRv3NonstopDebate> {
            withFormats(context) {
                val flow = dataSource.openInputFlow().doOnFailure { return it.cast() }

                use(flow) {
                    val timeLimit = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sectionCount = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val unk1 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk2 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk3 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk4 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val sectionBuffer = ByteArray(404) //Yes, this is the actual count

                    val sections = Array(sectionCount) {
                        println(flow.position())
                        flow.readExact(sectionBuffer) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        DRv3NonstopDebateSection.fromData(sectionBuffer)
                    }

                    return KorneaResult.Success(DRv3NonstopDebate(timeLimit, unk1, unk2, unk3, unk4, sections))
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

@ExperimentalUnsignedTypes
suspend fun SpiralContext.DRv3NonstopDebate(dataSource: DataSource<*>) = DRv3NonstopDebate(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDRv3NonstopDebate(dataSource: DataSource<*>) = DRv3NonstopDebate(this, dataSource).get()