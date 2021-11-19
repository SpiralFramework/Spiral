package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext

@ExperimentalUnsignedTypes
data class RSFSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24525346
    }

    override suspend fun setup(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<BaseSrdEntry> =
        KorneaResult.success(this@RSFSrdEntry)

    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {}
    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {}
}