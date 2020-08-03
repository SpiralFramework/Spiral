package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.io.common.DataSource

@ExperimentalUnsignedTypes
data class SCNSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x2453434E
    }
}