package info.spiralframework.formats.text

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readNullTerminatedString
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.formats.utils.useAt
import java.io.InputStream
import kotlin.collections.set

class STX private constructor(context: SpiralContext, val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x54585453
        
        operator fun invoke(context: SpiralContext, dataSource: DataSource): STX? {
            withFormats(context) {
                try {
                    return STX(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.stx.invalid", dataSource, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, dataSource: DataSource): STX = withFormats(context) { STX(this, dataSource) }
    }

    enum class Language(val langID: Int) {
        JPLL(0x4c4c504a),
        UNK(-1);

        companion object {
            fun languageFor(id: Int): Language? = values().firstOrNull { lang -> lang.langID == id }
        }
    }

    val strings: Map<Int, String>
    val lang: Language

    init {
        with(context) {
            val stream = dataSource()

            try {
                val magic = stream.readInt32LE()
                require(magic == MAGIC_NUMBER) { localise("formats.stx.invalid_magic", magic, MAGIC_NUMBER) }

                lang = Language.languageFor(stream.readInt32LE()) ?: Language.UNK

                val unk = stream.readInt32LE()
                if (unk != 1)
                    debug("formats.stx.unknown_unk", unk)

                val tableOffset = stream.readInt32LE()

                val unk2 = stream.readInt32LE()
                if (unk2 != 8)
                    debug("formats.stx.unknown_unk2", unk2)

                val count = stream.readInt32LE()

                strings = HashMap<Int, String>().apply {
                    dataSource.useAt(tableOffset) { stringStream ->
                        for (i in 0 until count) {
                            val stringID = stringStream.readInt32LE()
                            val stringOffset = stringStream.readInt32LE()

                            if (i != stringID)
                                debug("formats.stx.differing_id", stringID, i)

                            this[stringID] = dataSource.useAt(stringOffset) { localStream -> localStream.readNullTerminatedString(encoding = Charsets.UTF_16LE, bytesPer = 2) }
                        }
                    }
                }
            } finally {
                stream.close()
            }
        }
    }
}