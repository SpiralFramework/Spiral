package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.readInt16LE
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readUInt32LE
import java.io.InputStream

class AWB(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x32534641
    }

    val entries: Array<AWBEntry>

    init {
        val stream = dataSource()

        try {
            val magic = stream.readInt32LE()
            assert(magic == MAGIC_NUMBER)

            val unk1 = stream.readInt32LE()

            val numEntries = stream.readInt32LE()
            val alignment = stream.readInt32LE()

            val awbFileIDs = IntArray(numEntries) { stream.readInt16LE() }
            val headerEnd = stream.readUInt32LE()

            val awbFileEnds = LongArray(numEntries) { stream.readUInt32LE() }

            var start: Long
            var end: Long = headerEnd

            entries = Array(numEntries) { index ->
                start = end

                if (end % alignment > 0)
                    start += (alignment - (end % alignment))
                end = awbFileEnds[index]

                return@Array AWBEntry(awbFileIDs[index], end - start, start)
            }
        } finally {
            stream.close()
        }
    }
}