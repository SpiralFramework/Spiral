package info.spiralframework.formats.archives.srd

import info.spiralframework.formats.archives.SRD
import info.spiralframework.formats.utils.FaceBlock
import info.spiralframework.formats.utils.VertexBlock
import info.spiralframework.formats.utils.readInt16LE
import info.spiralframework.formats.utils.readInt32LE

open class VTXEntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
    val meshTypeID: Int
    val meshType: EnumSRDIMeshType
    val vertexCount: Int
    override val rsiEntry: RSIEntry = super.rsiEntry!!

    val vertexBlock: VertexBlock
        get() = rsiEntry.mipmaps[0]

    val faceBlock: FaceBlock
        get() = rsiEntry.mipmaps[1]


    init {
        val data = dataStream

        try {
            data.skip(0x06)
            meshTypeID = data.readInt16LE()
            meshType = EnumSRDIMeshType.meshTypeForID(meshTypeID)

            vertexCount = data.readInt32LE()
        } finally {
            data.close()
        }
    }
}