package org.abimon.spiral.core.objects.archives.srd

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.utils.WindowedInputStream
import org.abimon.spiral.core.utils.readNullTerminatedString

open class TXIEntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
    val filename: String
    val fileID: String
        get() = rsiEntry.name
    val rsiEntry: RSIEntry

    init {
        val stream = dataStream

        try {
            //Presumably, the header goes <number> <offset> but it never differs.
            //TODO: Fix this if it breaks
            stream.skip(20)

            filename = stream.readNullTerminatedString()
            rsiEntry = (subdataStream as WindowedInputStream).use { substream -> SRDEntry(substream, srd) } as RSIEntry
        } finally {
            stream.close()
        }
    }
}