package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.bookmark
import info.spiralframework.base.common.io.flow.readBytes
import info.spiralframework.base.common.io.readInt32LE
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.io.use
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue

@ExperimentalUnsignedTypes
/** Texture Information? */
data class TXISrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24545849
    }

    var textureNames: Array<String> by oneTimeMutable()
    var rsiEntry: RSISrdEntry by oneTimeMutable()

    val fileID: String
        get() = rsiEntry.name

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup() {
        rsiEntry = RSISrdEntry.unsafe(this, openSubDataSource())

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            requireNotNull(dataSource.openInputFlow()).use { setup(it) }
        else {
            setup(BinaryInputFlow(requireNotNull(dataSource.openInputFlow()).use { it.readBytes() }))
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow) {
        requireNotNull(flow.seek(0, InputFlow.FROM_BEGINNING))

        val textureCount = requireNotNull(flow.readInt32LE())
        val textureNameOffset = requireNotNull(flow.readInt32LE())

        flow.seek(textureNameOffset.toLong(), InputFlow.FROM_BEGINNING)
        textureNames = Array(textureCount) {
            val nameOffset = requireNotNull(flow.readInt32LE())
            bookmark(flow) {
                flow.seek(nameOffset.toLong(), InputFlow.FROM_BEGINNING)
                flow.readNullTerminatedUTF8String()
            }
        }
    }
}