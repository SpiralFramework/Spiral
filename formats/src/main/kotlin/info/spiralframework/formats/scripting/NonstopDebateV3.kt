package info.spiralframework.formats.scripting

import info.spiralframework.base.assertAsLocaleArgument
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.foldToInt16LE
import info.spiralframework.formats.utils.readInt16LE
import java.io.InputStream

class NonstopDebateV3 private constructor(val game: V3, val dataSource: () -> InputStream) {
    companion object {
        operator fun invoke(game: V3, dataSource: () -> InputStream): NonstopDebateV3? {
            try {
                return NonstopDebateV3(game, dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.nonstop_v3.invalid", dataSource, game, iae)

                return null
            }
        }

        fun unsafe(game: V3, dataSource: () -> InputStream): NonstopDebateV3 = NonstopDebateV3(game, dataSource)
    }
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
                assertAsLocaleArgument(read == sectionBuffer.size, "formats.nonstop_v3.invalid_stream_size", read, sectionBuffer.size)

                return@Array NonstopDebateSection(sectionBuffer.foldToInt16LE())
            }

            assertAsLocaleArgument(stream.read() == -1, "formats.nonstop_v3.invalid_stream_data")
        } finally {
            stream.close()
        }
    }
}