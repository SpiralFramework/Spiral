package info.spiralframework.formats.archives

import info.spiralframework.base.CountingInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readString
import info.spiralframework.base.util.readUInt32LE
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.DataSource

class SPC private constructor(context: SpiralContext, val dataSource: DataSource) : IArchive {
    companion object {
        val MAGIC_NUMBER = 0x2e535043
        val TABLE_MAGIC_NUMBER = 0x746f6f52

        val COMPRESSION_FLAG_ARRAYS = arrayOf(0x01, 0x02, 0x03)

        operator fun invoke(context: SpiralContext, dataSource: DataSource): SPC? {
            withFormats(context) {
                try {
                    return SPC(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.spc.invalid", dataSource, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, dataSource: DataSource): SPC = withFormats(context) { SPC(this, dataSource) }
    }

    val files: Array<SPCEntry>

    init {
        with(context) {
            val stream = CountingInputStream(dataSource())

            try {
                val magic = stream.readInt32LE()
                require(magic == MAGIC_NUMBER) { localise("formats.spc.invalid_magic", magic, MAGIC_NUMBER) }
                stream.skip(0x24)

                val fileCount = stream.readInt32LE()
                val unk2 = stream.readInt32LE()
                stream.skip(0x10)

                val tableMagic = stream.readInt32LE()
                require(tableMagic == TABLE_MAGIC_NUMBER) { localise("formats.spc.invalid_table_magic", tableMagic, TABLE_MAGIC_NUMBER) }
                stream.skip(0x0C)

                files = Array<SPCEntry>(fileCount) { index ->
                    val compressionFlag = stream.readInt16LE()
                    val unknownFlag = stream.readInt16LE()

                    val compressedSize = stream.readUInt32LE().toLong()
                    val decompressedSize = stream.readUInt32LE().toLong()
                    val nameLength = stream.readInt32LE() + 1

                    assert(compressionFlag in COMPRESSION_FLAG_ARRAYS)

                    stream.skip(0x10)

                    val namePadding = (0x10 - nameLength % 0x10) % 0x10
                    val dataPadding = (0x10 - compressedSize % 0x10) % 0x10

                    val name = stream.readString(nameLength - 1)
                    stream.skip(namePadding + 1L)

                    val position = stream.streamOffset

                    stream.skip(compressedSize + dataPadding)

                    return@Array SPCEntry(this, compressionFlag, unknownFlag, compressedSize, decompressedSize, name, position, this@SPC)
                }
            } finally {
                stream.close()
            }
        }
    }
}