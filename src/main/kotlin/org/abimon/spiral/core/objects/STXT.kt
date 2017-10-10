package org.abimon.spiral.core.objects

import org.abimon.spiral.core.readString
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.spiral.core.readZeroString
import org.abimon.spiral.util.SeekableInputStream
import org.abimon.visi.io.DataSource

class STXT(val dataSource: DataSource) {
    companion object {
        val STXT_MAGIC = "STXT"
    }

    val strings: Map<Int, String>
    val lang: String


    init {
        val stream = SeekableInputStream(dataSource.seekableInputStream)

        try {
            val magic = stream.readString(4)
            if(magic != STXT_MAGIC)
                throw IllegalArgumentException()

            lang = stream.readString(4)

            val unk = stream.readUnsignedLittleInt()
            val tableOffset = stream.readUnsignedLittleInt()

            val unk2 = stream.readUnsignedLittleInt()
            val count = stream.readUnsignedLittleInt()

            val strs = HashMap<Int, String>()

            for(i in 0 until count) {
                stream.seek(tableOffset + (i * 8))

                val stringID = stream.readUnsignedLittleInt()
                val stringOffset = stream.readUnsignedLittleInt()

                stream.seek(stringOffset)

                strs[stringID.toInt()] = stream.readZeroString(encoding = "UTF-16LE", bytesPerCharacter = 2)
            }

            strings = strs
        } finally {
            stream.close()
        }
    }
}