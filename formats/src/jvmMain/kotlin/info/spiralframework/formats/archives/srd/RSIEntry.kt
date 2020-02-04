package info.spiralframework.formats.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readNullTerminatedString
import info.spiralframework.formats.archives.SRD
import info.spiralframework.formats.utils.Mipmap
import org.abimon.kornea.io.jvm.CountingInputStream

open class RSIEntry(context: SpiralContext, dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(context, dataType, offset, dataLength, subdataLength, srd) {
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
        val stream = CountingInputStream(dataStream)
        try {
            unk1 = stream.readInt16LE()
            unk2 = stream.read() and 0xFF
            mipmapCount = stream.read() and 0xFF
            unk3 = stream.readInt32LE()
            unk4 = stream.readInt32LE()

            nameOffset = stream.readInt32LE()

            mipmaps = Array(mipmapCount) { Mipmap(stream.readInt32LE() and 0x0FFFFFFF, stream.readInt32LE(), stream.readInt32LE(), stream.readInt32LE()) }
        } finally {
            stream.close()
        }

        name = dataStream.use { nameStream ->
            nameStream.skip(nameOffset.toLong())
            nameStream.readNullTerminatedString()
        }
    }
}