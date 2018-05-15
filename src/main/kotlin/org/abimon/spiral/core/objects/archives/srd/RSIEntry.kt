package org.abimon.spiral.core.objects.archives.srd

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.utils.Mipmap
import org.abimon.spiral.core.utils.readInt16LE
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readNullTerminatedString

open class RSIEntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
    data class ResourceArray(val start: Int, val length: Int, val unk1: Int, val unk2: Int)
    override val rsiEntry: RSIEntry? = null

    val unk1: Int
    val unk2: Int
    val mipmapCount: Int
    val unk3: Int
    val unk4: Int
    val nameOffset: Int

    val mipmaps: Array<Mipmap>
    val name: String

    init {
        val stream = dataStream
        try {
            unk1 = stream.readInt16LE()
            unk2 = stream.read() and 0xFF
            mipmapCount = stream.read() and 0xFF
            unk3 = stream.readInt32LE()
            unk4 = stream.readInt32LE()

            nameOffset = stream.readInt32LE()

            mipmaps = Array(mipmapCount) { Mipmap(stream.readInt32LE() and 0x00FFFFFF, stream.readInt32LE(), stream.readInt32LE(), stream.readInt32LE()) }
        } finally {
            stream.close()
        }

        name = dataStream.use { nameStream ->
            nameStream.skip(nameOffset.toLong())
            nameStream.readNullTerminatedString()
        }
    }
}