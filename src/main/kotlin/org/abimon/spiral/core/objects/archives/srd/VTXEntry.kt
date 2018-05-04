package org.abimon.spiral.core.objects.archives.srd

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.utils.*

open class VTXEntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
    val meshTypeID: Int
    val meshType: EnumSRDIMeshType
    val vertexCount: Int
    val rsiEntry: RSIEntry

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

            rsiEntry = (subdataStream as WindowedInputStream).use { substream -> SRDEntry(substream, srd) } as RSIEntry
        } finally {
            data.close()
        }
    }
}