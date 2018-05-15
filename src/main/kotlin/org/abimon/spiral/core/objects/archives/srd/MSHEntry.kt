package org.abimon.spiral.core.objects.archives.srd

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.utils.readInt16LE
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readNullTerminatedString

open class MSHEntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
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