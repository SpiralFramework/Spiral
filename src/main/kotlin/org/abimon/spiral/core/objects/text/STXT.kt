package org.abimon.spiral.core.objects.text

import org.abimon.spiral.core.utils.*
import java.io.InputStream
import kotlin.collections.set

class STXT(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x54585453
    }

    val strings: Map<Int, String>
    val lang: String


    init {
        val stream = dataSource()

        try {
            val magic = stream.readInt32LE()
            assertAsArgument(magic == MAGIC_NUMBER, "Illegal magic number in STXT File (Was $magic, expected $MAGIC_NUMBER)")

            lang = stream.readString(4)

            val unk = stream.readInt32LE()
            val tableOffset = stream.readInt32LE()

            val unk2 = stream.readInt32LE()
            val count = stream.readInt32LE()

            strings = HashMap<Int, String>().apply {

                for (i in 0 until count) {
                    dataSource.useAt(tableOffset + (i * 8)) { stringStream ->
                        val stringID = stringStream.readInt32LE()
                        val stringOffset = stringStream.readInt32LE()

                        this[stringID] = dataSource.useAt(stringOffset) { localStream -> localStream.readNullTerminatedString(encoding = Charsets.UTF_16LE, bytesPer = 2) }
                    }
                }
            }
        } finally {
            stream.close()
        }
    }
}