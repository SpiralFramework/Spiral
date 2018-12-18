package info.spiralframework.formats.scripting

import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.utils.assertAsArgument
import info.spiralframework.formats.utils.foldToInt16LE
import info.spiralframework.formats.utils.readInt16LE
import java.io.InputStream

class NonstopDebateV3(val game: V3, val dataSource: () -> InputStream) {
    val timeLimit: Int
    val unk: Int
    val otherBitsOfHeader: ByteArray
    val sections: Array<NonstopDebateSection>

    init {
        val stream = dataSource()

        try {
            timeLimit = stream.readInt16LE()

            val sectionCount = stream.read()
            unk = stream.read()

            otherBitsOfHeader = ByteArray(8)
            stream.read(otherBitsOfHeader)

            val sectionBuffer = ByteArray(404)

            sections = Array(sectionCount) {
                val read = stream.read(sectionBuffer)
                assertAsArgument(read == sectionBuffer.size, "Illegal stream size for Nonstop Debate (Expected to read ${sectionBuffer.size}, instead we read $read bytes)")

                return@Array NonstopDebateSection(sectionBuffer.foldToInt16LE())
            }
        } finally {
            stream.close()
        }
    }
}