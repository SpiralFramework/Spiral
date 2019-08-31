package info.spiralframework.formats.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.archives.SRD
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readNullTerminatedUTF8String
import info.spiralframework.formats.utils.useAt
import java.io.InputStream

open class TXIEntry(context: SpiralContext, dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(context, dataType, offset, dataLength, subdataLength, srd) {
    val fileFrames: Array<String>
    val filename: String
        get() = fileFrames[0]
    val fileID: String
        get() = rsiEntry.name
    override val rsiEntry: RSIEntry = super.rsiEntry!!

    init {
        val stream = dataStream

        try {
            stream.skip(12)
            val filenameCounts = stream.readInt32LE() and 0x000000FF

            fileFrames = Array(filenameCounts) { (this::dataStream).useAt(stream.readInt32LE(), InputStream::readNullTerminatedUTF8String) }
        } finally {
            stream.close()
        }
    }
}