package info.spiralframework.formats.scripting

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readInt64LE
import info.spiralframework.base.util.readXBytes
import info.spiralframework.formats.common.NULL_TERMINATOR
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.DataSource
import java.io.InputStream

class DR1SaveFile private constructor(context: SpiralContext, val dataSource: DataSource) {
    companion object {
        const val MAGIC_NUMBER = 0x617461640000000C
        const val BIN_MAGIC_NUMBER = 0x3AC946E69622E.toInt()
        val FILE_NUMBER_RANGE = 0..30

        fun Int.convertASCIIToInt(): Int {
            val a = ((this shr 0) and 0xFF) % 48
            val b = ((this shr 8) and 0xFF) % 48
            val c = ((this shr 16) and 0xFF) % 48
            val d = ((this shr 24) and 0xFF) % 48

            return (a * 1000) + (b * 100) + (c * 10) + (d * 1)
        }

        operator fun invoke(context: SpiralContext, dataSource: () -> InputStream): DR1SaveFile? {
            withFormats(context) {
                try {
                    return DR1SaveFile(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.save.invalid", dataSource, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, dataSource: () -> InputStream): DR1SaveFile = withFormats(context) { DR1SaveFile(this, dataSource) }

    }

    val size: Int
    val danganronpaString: String
    val chapterSectionString: String
    val debugInfoString: String
    val lastPlayedString: String

    init {
        with(context) {
            val stream = dataSource()

            try {
                val magic = stream.readInt64LE()
                require(magic == MAGIC_NUMBER) { localise("formats.save.dr1") }

                val fileNumber = stream.readInt32LE().convertASCIIToInt()
                require(fileNumber in FILE_NUMBER_RANGE) { localise("formats.save.dr1.range") }

                val binMagic = stream.readInt32LE()
                require(binMagic == BIN_MAGIC_NUMBER) { localise("formats.save.dr1.bin") }

                size = stream.readInt32LE()
                danganronpaString = String(stream.readXBytes(64), Charsets.US_ASCII).trimEnd(NULL_TERMINATOR)
                chapterSectionString = String(stream.readXBytes(128), Charsets.US_ASCII).trimEnd(NULL_TERMINATOR)
                debugInfoString = String(stream.readXBytes(512), Charsets.US_ASCII).trimEnd(NULL_TERMINATOR)

                lastPlayedString = String(stream.readXBytes(32), Charsets.US_ASCII).trimEnd(NULL_TERMINATOR)

                //D880: coins
            } finally {
                stream.close()
            }
        }
    }
}