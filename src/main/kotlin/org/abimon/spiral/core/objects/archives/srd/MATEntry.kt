package org.abimon.spiral.core.objects.archives.srd

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.utils.readInt16LE
import org.abimon.spiral.core.utils.readNullTerminatedString

open class MATEntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
    val materials: Map<String, String>
    override val rsiEntry: RSIEntry = super.rsiEntry!!
    
    init {
        val stream = dataStream

        try {
            stream.skip(20)

            val materialsOffset = stream.readInt16LE()
            val materialsCount = stream.readInt16LE()

            materials = dataStream.use { materialStream ->
                materialStream.skip(materialsOffset.toLong())
                val map = HashMap<String, String>()

                for (i in 0 until materialsCount) {
                    val textureNameOffset = materialStream.readInt16LE()
                    val materialTypeOffset = materialStream.readInt16LE()

                    val textureName = dataStream.use { nameStream ->
                        nameStream.skip(textureNameOffset.toLong())
                        nameStream.readNullTerminatedString()
                    }

                    val materialType = dataStream.use { nameStream ->
                        nameStream.skip(materialTypeOffset.toLong())
                        nameStream.readNullTerminatedString()
                    }

                    map[materialType] = textureName
                }

                return@use map
            }
        } finally {
            stream.close()
        }
    }
}