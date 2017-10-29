package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.readNumber
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.spiral.util.CountingInputStream
import org.abimon.visi.io.DataSource

class SPC(val dataSource: DataSource) {
    companion object {
        val SPC_MAGIC = "CPS."
        val SPC_TABLE_MAGIC = "Root"
    }

    val files: MutableList<SPCFileEntry> = ArrayList()

    init {
        dataSource.seekableUse {
            val stream = CountingInputStream(it)
            val magic = stream.readString(4)
            if(magic != SPC_MAGIC)
                throw IllegalArgumentException("${dataSource.location} is an invalid/corrupt SPC file! (Magic $magic ≠ 'CPS.')")

            stream.skip(0x24)
            val fileCount = stream.readUnsignedLittleInt()
            val unk2 = stream.readUnsignedLittleInt()

            stream.skip(0x10)

            val tableMagic = stream.readString(4)
            if(tableMagic != SPC_TABLE_MAGIC)
                throw IllegalArgumentException("${dataSource.location} is an invalid/corrupt SPC file! (Table Magic $tableMagic ≠ 'Root')")

            stream.skip(0x0C)

            for(i in 0 until fileCount) {
                val cmp_flag = stream.readNumber(2, unsigned = true, little = true)
                val unk_flag = stream.readNumber(2, unsigned = true, little = true)
                val cmp_size = stream.readUnsignedLittleInt()
                val dec_size = stream.readUnsignedLittleInt()
                val name_len = stream.readUnsignedLittleInt() + 1

                if(cmp_flag !in arrayOf(0x01L, 0x02L, 0x03L))
                    throw IllegalArgumentException("${dataSource.location} is an invalid/corrupt SPC File! (Unknown cmp flag $cmp_flag)")

                stream.skip(0x10)

                val name_padding = (0x10 - name_len % 0x10) % 0x10
                val data_padding = (0x10 - cmp_size % 0x10) % 0x10

                val name = stream.readString(name_len.toInt() - 1)
                stream.skip(name_padding + 1)

                files.add(SPCFileEntry(cmp_flag.toInt(), unk_flag.toInt(), cmp_size, dec_size, name, stream.count, this))
                stream.skip(cmp_size + data_padding)
            }
        }
    }
}