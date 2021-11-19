package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext

@ExperimentalUnsignedTypes
data class UnknownSrdEntry(override val classifier: Int, override val mainDataLength: ULong, override val subDataLength: ULong, override val unknown: Int) :
    BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown) {
    override suspend fun setup(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<BaseSrdEntry> =
        KorneaResult.success(this@UnknownSrdEntry)

    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {
        TODO("Not yet implemented")
    }

    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {
        TODO("Not yet implemented")
    }
}