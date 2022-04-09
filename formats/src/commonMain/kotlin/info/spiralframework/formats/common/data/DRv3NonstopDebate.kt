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
import kotlin.math.round

public class DRv3NonstopDebate(
    public val baseTimeLimit: Int,
    public val unk1: Int,
    public val unk2: Int,
    public val unk3: Int,
    public val unk4: Int,
    public val sections: Array<DRv3NonstopDebateSection>
) {
    public companion object {
        public const val NOT_ENOUGH_DATA_KEY: String = "formats.nonstop_debate.drv3.not_enough_data"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<DRv3NonstopDebate> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return it.cast() }

                closeAfter(flow) {
                    val timeLimit = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val sectionCount =
                        flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val unk1 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk2 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk3 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk4 = flow.readInt16LE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val sectionBuffer = ByteArray(404) //Yes, this is the actual count

                    val sections = Array(sectionCount) {
                        println(flow.position())
                        flow.readExact(sectionBuffer) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        DRv3NonstopDebateSection.fromData(sectionBuffer)
                    }

                    return@closeAfter KorneaResult.success(
                        DRv3NonstopDebate(
                            timeLimit,
                            unk1,
                            unk2,
                            unk3,
                            unk4,
                            sections
                        )
                    )
                }
            }
    }

    /** 2 * timeLimit */
    public val gentleTimeLimit: Int = baseTimeLimit * 2

    /** 1 * timeLimit */
    public val kindTimeLimit: Int = baseTimeLimit * 1

    /** 0.8 * timeLimit */
    public val meanTimeLimit: Int = round(baseTimeLimit * 0.8).toInt()
}

@Suppress("FunctionName")
public suspend fun SpiralContext.DRv3NonstopDebate(dataSource: DataSource<*>): KorneaResult<DRv3NonstopDebate> =
    DRv3NonstopDebate(this, dataSource)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeDRv3NonstopDebate(dataSource: DataSource<*>): DRv3NonstopDebate =
    DRv3NonstopDebate(this, dataSource).getOrThrow()