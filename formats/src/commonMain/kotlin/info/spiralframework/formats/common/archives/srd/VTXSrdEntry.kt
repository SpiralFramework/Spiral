package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.readAndClose
import info.spiralframework.base.common.io.readInt16LE
import info.spiralframework.base.common.io.readInt32LE
import info.spiralframework.base.common.io.use
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue
import info.spiralframework.formats.common.models.SrdiMeshType

typealias VertexBlock = RSISrdEntry.ResourceIndex
typealias IndexBlock = RSISrdEntry.ResourceIndex
typealias FaceBlock = RSISrdEntry.ResourceIndex

@ExperimentalUnsignedTypes
data class VTXSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24565458
    }

    var rsiEntry: RSISrdEntry by oneTimeMutable()
    val vertexBlock: VertexBlock
        get() = rsiEntry.resources[0]
    val faceBlock: FaceBlock
        get() = rsiEntry.resources[1]

    var unk1: Int by oneTimeMutable()
    var unk2: Int by oneTimeMutable()
    var meshType: SrdiMeshType by oneTimeMutable()
    var vertexCount: Int by oneTimeMutable()

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup() {
        rsiEntry = RSISrdEntry.unsafe(this, openSubDataSource())

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            requireNotNull(dataSource.openInputFlow()).use { setup(it) }
        else {
            setup(BinaryInputFlow(requireNotNull(dataSource.openInputFlow()).readAndClose()))
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow) {
        requireNotNull(flow.seek(0, InputFlow.FROM_BEGINNING))

        unk1 = requireNotNull(flow.readInt32LE())
        unk2 = requireNotNull(flow.readInt16LE())
        meshType = SrdiMeshType(requireNotNull(flow.readInt16LE()))
        vertexCount = requireNotNull(flow.readInt32LE())

        //There's more data after this, I'm not gonna try that yet. Seems to be pairs of shorts?
    }
}