package org.abimon.spiral.core.objects.archives.srd

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readNullTerminatedUTF8String
import org.abimon.spiral.core.utils.useAt
import java.io.InputStream

open class TXIEntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
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