package info.spiralframework.formats.video

import info.spiralframework.base.CountingInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.video.sfl.SFLTable
import java.io.InputStream

class SFL private constructor(context: SpiralContext, val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x53464C4C
        
        operator fun invoke(context: SpiralContext, dataSource: DataSource): SFL? {
            withFormats(context) {
                try {
                    return SFL(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.sfl.invalid", dataSource, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, dataSource: DataSource): SFL = withFormats(context) { SFL(this, dataSource) }
    }

    val headerUnk1: Int
    val headerUnk2: Int
    val maxTableIndex: Int

    val tables: Array<SFLTable>

    init {
        with(context) {
            val stream = CountingInputStream(dataSource())

            try {
                val magic = stream.readInt32LE()
                require(magic == MAGIC_NUMBER) { localise("formats.sfl.invalid_magic", "0x${magic.toString(16)}", "0x${MAGIC_NUMBER.toString(16)}") }

                headerUnk1 = stream.readInt32LE()
                headerUnk2 = stream.readInt32LE()
                maxTableIndex = stream.readInt32LE()

                if (headerUnk2 != 7)
                    debug("formats.sfl.headerUnk2", headerUnk2)

                if (maxTableIndex != 5 && maxTableIndex != 6)
                    debug("formats.sfl.max_table_index", maxTableIndex)

                tables = Array(maxTableIndex) {
                    val table = SFLTable(stream.readInt32LE(), stream.readInt32LE().toInt(), stream.readInt32LE(), stream.readInt32LE(), stream.streamOffset.toInt(), this@SFL)
                    stream.skip(table.length)
                    return@Array table
                }
            } finally {
                stream.close()
            }
        }
    }
}