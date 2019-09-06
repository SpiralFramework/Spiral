package info.spiralframework.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readUInt32LE
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.DataSource

@ExperimentalUnsignedTypes
class AWB private constructor(context: SpiralContext, val dataSource: DataSource) : IArchive {
    companion object {
        val MAGIC_NUMBER = 0x32534641

        operator fun invoke(context: SpiralContext, dataSource: DataSource): AWB? {
            withFormats(context) {
                try {
                    return AWB(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.awb.invalid", dataSource, iae)

                    return null
                }
            }
        }
        fun unsafe(context: SpiralContext, dataSource: DataSource): AWB = withFormats(context) { AWB(this, dataSource) }
    }

    val entries: Array<AWBEntry>

    init {
        with(context) {
            val stream = dataSource()

            try {
                val magic = stream.readInt32LE()
                require(magic == MAGIC_NUMBER) { localise("formats.awb.invalid_magic", magic, MAGIC_NUMBER) }

                val unk1 = stream.readInt32LE()

                val numEntries = stream.readInt32LE()
                val alignment = stream.readInt32LE()

                val awbFileIDs = IntArray(numEntries) { stream.readInt16LE() }
                val headerEnd = stream.readUInt32LE().toLong()

                val awbFileEnds = LongArray(numEntries) { stream.readUInt32LE().toLong() }

                var start: Long
                var end: Long = headerEnd

                entries = Array(numEntries) { index ->
                    start = end

                    if (end % alignment > 0)
                        start += (alignment - (end % alignment))
                    end = awbFileEnds[index]

                    return@Array AWBEntry(awbFileIDs[index], end - start, start, this@AWB)
                }
            } finally {
                stream.close()
            }
        }
    }
}