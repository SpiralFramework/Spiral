package info.spiralframework.formats.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.archives.SRD
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readNullTerminatedString

open class MSHEntry(context: SpiralContext, dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(context, dataType, offset, dataLength, subdataLength, srd) {
    override val rsiEntry: RSIEntry = super.rsiEntry!!

    val meshName: String
    val materialName: String

    init {
        val stream = dataStream

        try {
            val unk = stream.readInt32LE()

            val meshNameOffset = stream.readInt16LE()
            val materialNameOffset = stream.readInt16LE()

            meshName = dataStream.use { nameStream ->
                nameStream.skip(meshNameOffset.toLong())
                nameStream.readNullTerminatedString()
            }

            materialName = dataStream.use { nameStream ->
                nameStream.skip(materialNameOffset.toLong())
                nameStream.readNullTerminatedString()
            }
        } finally {
            stream.close()
        }
    }
}