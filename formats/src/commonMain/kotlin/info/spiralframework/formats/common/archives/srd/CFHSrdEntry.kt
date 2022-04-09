package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext

public data class CFHSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown) {
    public companion object {
        public const val MAGIC_NUMBER_BE: Int = 0x24434648
    }

    override suspend fun setup(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<BaseSrdEntry> = KorneaResult.success(this@CFHSrdEntry)

    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {}
    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {}
}