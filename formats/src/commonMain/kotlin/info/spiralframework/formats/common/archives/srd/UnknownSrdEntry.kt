package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext

@ExperimentalUnsignedTypes
data class UnknownSrdEntry(override val classifier: Int, override val mainDataLength: ULong, override val subDataLength: ULong, override val unknown: Int, override val dataSource: DataSource<*>) :
    BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    override suspend fun SpiralContext.setup(): KorneaResult<BaseSrdEntry> =
        KorneaResult.success(this@UnknownSrdEntry)
}