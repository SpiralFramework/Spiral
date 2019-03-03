package info.spiralframework.formats.archives

import info.spiralframework.base.util.assertAsLocaleArgument
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readUInt32LE
import info.spiralframework.formats.utils.DataHandler
import java.io.InputStream

class AWB private constructor(val dataSource: () -> InputStream): IArchive {
    companion object {
        val MAGIC_NUMBER = 0x32534641

        operator fun invoke(dataSource: () -> InputStream): AWB? {
            try {
                return AWB(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.awb.invalid", dataSource, iae)

                return null
            }
        }

        fun unsafe(dataSource: () -> InputStream): AWB = AWB(dataSource)
    }

    val entries: Array<AWBEntry>

    init {
        val stream = dataSource()

        try {
            val magic = stream.readInt32LE()
            assertAsLocaleArgument(magic == MAGIC_NUMBER, "formats.awb.invalid_magic", magic, MAGIC_NUMBER)

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

                return@Array AWBEntry(awbFileIDs[index], end - start, start, this)
            }
        } finally {
            stream.close()
        }
    }
}