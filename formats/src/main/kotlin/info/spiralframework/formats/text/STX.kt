package info.spiralframework.formats.text

import info.spiralframework.base.util.assertAsLocaleArgument
import info.spiralframework.formats.utils.*
import java.io.InputStream
import kotlin.collections.set

class STX private constructor(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x54585453
        
        operator fun invoke(dataSource: DataSource): STX? {
            try {
                return STX(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.stx.invalid", dataSource, iae)

                return null
            }
        }

        fun unsafe(dataSource: DataSource): STX = STX(dataSource)
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
        val stream = dataSource()

        try {
            val magic = stream.readInt32LE()
            assertAsLocaleArgument(magic == MAGIC_NUMBER, "formats.stx.invalid_magic", magic, MAGIC_NUMBER)

            lang = Language.languageFor(stream.readInt32LE()) ?: Language.UNK

            val unk = stream.readInt32LE()
            if (unk != 1)
                DataHandler.LOGGER.debug("formats.stx.unknown_unk", unk)

            val tableOffset = stream.readInt32LE()

            val unk2 = stream.readInt32LE()
            if (unk2 != 8)
                DataHandler.LOGGER.debug("formats.stx.unknown_unk2", unk2)

            val count = stream.readInt32LE()

            strings = HashMap<Int, String>().apply {
                dataSource.useAt(tableOffset) { stringStream ->
                    for (i in 0 until count) {
                        val stringID = stringStream.readInt32LE()
                        val stringOffset = stringStream.readInt32LE()

                        if (i != stringID)
                            DataHandler.LOGGER.debug("formats.stx.differing_id", stringID, i)

                        this[stringID] = dataSource.useAt(stringOffset) { localStream -> localStream.readNullTerminatedString(encoding = Charsets.UTF_16LE, bytesPer = 2) }
                    }
                }
            }
        } finally {
            stream.close()
        }
    }
}