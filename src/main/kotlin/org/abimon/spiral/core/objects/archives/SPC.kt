package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.*
import java.io.InputStream

class SPC(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x2e535043
        val TABLE_MAGIC_NUMBER = 0x746f6f52

        val COMPRESSION_FLAG_ARRAYS = arrayOf(0x01, 0x02, 0x03)
    }

    val files: Array<SPCEntry>

    init {
        val stream = CountingInputStream(dataSource())

        try {
            val magic = stream.readInt32LE()
            assertAsArgument(magic == MAGIC_NUMBER, "Illegal magic number in SPC File (Was $magic, expected $MAGIC_NUMBER)")
            stream.skip(0x24)

            val fileCount = stream.readInt32LE()
            val unk2 = stream.readInt32LE()
            stream.skip(0x10)

            val tableMagic = stream.readInt32LE()
            assertAsArgument(tableMagic == TABLE_MAGIC_NUMBER, "Illegal magic number in SPC Table (Was $tableMagic, expected $TABLE_MAGIC_NUMBER)")
            stream.skip(0x0C)

            files = Array<SPCEntry>(fileCount) { index ->
                val compressionFlag = stream.readInt16LE()
                val unknownFlag = stream.readInt16LE()

                val compressedSize = stream.readUInt32LE()
                val decompressedSize = stream.readUInt32LE()
                val nameLength = stream.readInt32LE() + 1

                assert(compressionFlag in COMPRESSION_FLAG_ARRAYS)

                stream.skip(0x10)

                val namePadding = (0x10 - nameLength % 0x10) % 0x10
                val dataPadding = (0x10 - compressedSize % 0x10) % 0x10

                val name = stream.readString(nameLength - 1)
                stream.skip(namePadding + 1L)

                val position = stream.count

                stream.skip(compressedSize + dataPadding)

                return@Array SPCEntry(compressionFlag, unknownFlag, compressedSize, decompressedSize, name, position)
            }
        } finally {
            stream.close()
        }
    }
}