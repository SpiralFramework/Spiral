package info.spiralframework.formats.video

import info.spiralframework.base.CountingInputStream
import info.spiralframework.base.util.assertAsLocaleArgument
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.formats.video.sfl.SFLTable
import java.io.InputStream

class SFL private constructor(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x53464C4C
        
        operator fun invoke(dataSource: DataSource): SFL? {
            try {
                 return SFL(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.sfl.invalid", dataSource, iae)
                
                return null
            }
        }

        fun unsafe(dataSource: DataSource): SFL = SFL(dataSource)
    }

    val headerUnk1: Int
    val headerUnk2: Int
    val maxTableIndex: Int

    val tables: Array<SFLTable>

    init {
        val stream = CountingInputStream(dataSource())

        try {
            val magic = stream.readInt32LE()
            assertAsLocaleArgument(magic == MAGIC_NUMBER, "formats.sfl.invalid_magic", "0x${magic.toString(16)}", "0x${MAGIC_NUMBER.toString(16)}")

            headerUnk1 = stream.readInt32LE()
            headerUnk2 = stream.readInt32LE()
            maxTableIndex = stream.readInt32LE()

            if (headerUnk2 != 7)
                DataHandler.LOGGER.debug("formats.sfl.headerUnk2", headerUnk2)

            if (maxTableIndex != 5 && maxTableIndex != 6)
                DataHandler.LOGGER.debug("formats.sfl.max_table_index", maxTableIndex)

            tables = Array(maxTableIndex) {
                val table = SFLTable(stream.readInt32LE(), stream.readInt32LE().toLong(), stream.readInt32LE(), stream.readInt32LE(), stream.streamOffset, this)
                stream.skip(table.length)
                return@Array table
            }
        } finally {
            stream.close()
        }
    }
}