package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.bookmark
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

@ExperimentalUnsignedTypes
data class MeshSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x244D5348
    }

    var meshName: String by oneTimeMutable()
    var materialName: String by oneTimeMutable()

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup() {
        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            dataSource.openInputFlow()?.use { setup(it) }
        else {
            setup(BinaryInputFlow(dataSource.openInputFlow()?.use { it.readBytes() } ?: return))
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow) {
        val unk = flow.readInt32LE()

        val meshNameOffset = requireNotNull(flow.readInt16LE())
        val materialNameOffset = requireNotNull(flow.readInt16LE())

        bookmark(flow) {
            flow.seek(meshNameOffset.toLong(), InputFlow.FROM_BEGINNING)
            meshName = flow.readNullTerminatedUTF8String()

            flow.seek(materialNameOffset.toLong(), InputFlow.FROM_BEGINNING)
            materialName = flow.readNullTerminatedUTF8String()
        }
    }
}