package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.readShort
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.visi.io.DataSource

class AWB(val dataSource: DataSource) {
    companion object {
        val MAGIC = "AFS2"
    }

    val entries: Array<AWBEntry>

    init {
        val stream = dataSource.inputStream

        try {
            val magic = stream.readString(4)
            if(magic != MAGIC)
                throw IllegalArgumentException()

            val unk1 = stream.readUnsignedLittleInt()

            val numEntries = stream.readUnsignedLittleInt().toInt()
            val alignment = stream.readUnsignedLittleInt().toInt() //Chaotic Neutral

            val awbFileIDs = IntArray(numEntries)
            val awbFileEnds = LongArray(numEntries)

            for(i in 0 until numEntries)
                awbFileIDs[i] = stream.readShort(true, true)

            val headerEnd = stream.readUnsignedLittleInt()
            for(i in 0 until numEntries)
                awbFileEnds[i] = stream.readUnsignedLittleInt()

            val awbEntries: MutableList<AWBEntry> = ArrayList()

            var start: Long
            var end: Long = headerEnd

            for(i in 0 until numEntries) {
                start = end

                if(end % alignment > 0)
                    start += (alignment - (end % alignment))

                end = awbFileEnds[i]

                awbEntries.add(AWBEntry(awbFileIDs[i], end - start, start, this))
            }

            entries = awbEntries.toTypedArray()
        } finally {
            stream.close()
        }
    }
}