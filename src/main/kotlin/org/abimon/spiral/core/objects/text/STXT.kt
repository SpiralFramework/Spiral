package org.abimon.spiral.core.objects.text

import org.abimon.spiral.core.utils.*
import java.io.InputStream
import kotlin.collections.set

class STXT(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x54585453
    }

    enum class Language(val langID: Int) {
        JPLL(0x4c4c504a),
        UNK(-1);

        companion object {
            fun languageFor(id: Int): STXT.Language? = values().firstOrNull { lang -> lang.langID == id }
        }
    }

    val strings: Map<Int, String>
    val lang: STXT.Language

    init {
        val stream = dataSource()

        try {
            val magic = stream.readInt32LE()
            assertAsArgument(magic == MAGIC_NUMBER, "Illegal magic number in STXT File (Was $magic, expected $MAGIC_NUMBER)")

            lang = STXT.Language.languageFor(stream.readInt32LE()) ?: STXT.Language.UNK

            val unk = stream.readInt32LE()
            if (unk != 1)
                DataHandler.LOGGER.debug("STXT file $dataSource, for unk: expected 1, got $unk")

            val tableOffset = stream.readInt32LE()

            val unk2 = stream.readInt32LE()
            if (unk2 != 8)
                DataHandler.LOGGER.debug("STXT file $dataSource, for unk2: expected 8, got $unk")

            val count = stream.readInt32LE()

            strings = HashMap<Int, String>().apply {
                dataSource.useAt(tableOffset) { stringStream ->
                    for (i in 0 until count) {
                        val stringID = stringStream.readInt32LE()
                        val stringOffset = stringStream.readInt32LE()

                        if (i != stringID)
                            DataHandler.LOGGER.debug("STXT file $dataSource has a differing string ID (Index $i, string ID $stringID)")

                        this[stringID] = dataSource.useAt(stringOffset) { localStream -> localStream.readNullTerminatedString(encoding = Charsets.UTF_16LE, bytesPer = 2) }
                    }
                }
            }
        } finally {
            stream.close()
        }
    }
}