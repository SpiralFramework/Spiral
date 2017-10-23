package org.abimon.spiral.core.objects

import org.abimon.spiral.core.*
import org.abimon.spiral.util.CountingInputStream
import org.abimon.spiral.util.OffsetInputStream
import org.abimon.spiral.util.debug
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readPartialBytes

class GMOModel(val dataSource: DataSource) {

    init {
        dataSource.use { rawStream ->
            val stream = CountingInputStream(rawStream)
            val magic = stream.readString(12)
            assert(magic == "OMG.00.1PSP\u0000")
            val padding = stream.readInt()

            val chunks = stream.readChunks(dataSource, null)

            println(chunks)
        }
    }

    fun CountingInputStream.readChunks(dataSource: DataSource, parentChunk: GMOModelChunk?): List<GMOModelChunk> {
        val list: MutableList<GMOModelChunk> = ArrayList()

        try {
            while(available() > 0) {
                val chunkID = this.readUnsureShort(true, true) ?: break
                val headerSize = this.readUnsureShort(true, true) ?: break
                val dataSize = this.readUnsureInt(true, true) ?: break
                val header = if (headerSize <= 0) IntArray(0) else this.readPartialBytes(headerSize - 8).toIntArray()
                val chunk = GMOModelChunk(chunkID, headerSize, dataSize, header)
                val substream = OffsetInputStream(dataSource.seekableInputStream, streamOffset, dataSize - 8 - headerSize)

                list.add(chunk)

                when(chunkID) {
                    0x02 -> list.addAll(substream.readChunks(dataSource, chunk))
                    0x03 -> list.addAll(substream.readChunks(dataSource, chunk))
                    else -> debug("Missing chunk ID 0x${chunkID.toString(16)}")
                }

                if(dataSize > 0)
                    skip(dataSize - 8 - headerSize)
            }
        }
        catch(indexOutOfBounds: IndexOutOfBoundsException) {
            indexOutOfBounds.printStackTrace()
        }

        return list
    }
}