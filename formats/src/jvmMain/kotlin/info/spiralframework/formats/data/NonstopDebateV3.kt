package info.spiralframework.formats.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.utils.foldToInt16LE
import java.io.InputStream

class NonstopDebateV3 private constructor(context: SpiralContext, val game: V3, val dataSource: () -> InputStream) {
    companion object {
        operator fun invoke(context: SpiralContext, game: V3, dataSource: () -> InputStream): NonstopDebateV3? {
            withFormats(context) {
                try {
                    return NonstopDebateV3(this, game, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.nonstop_v3.invalid", dataSource, game, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, game: V3, dataSource: () -> InputStream): NonstopDebateV3 = withFormats(context) { NonstopDebateV3(this, game, dataSource) }
    }
    val timeLimit: Int
    val unk: Int
    val otherBitsOfHeader: ByteArray
    val sections: Array<NonstopDebateSection>

    init {
        with(context) {
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
                    require(read == sectionBuffer.size) { localise("formats.nonstop_v3.invalid_stream_size", read, sectionBuffer.size) }

                    return@Array NonstopDebateSection(sectionBuffer.foldToInt16LE())
                }

                require(stream.read() == -1) { localise("formats.nonstop_v3.invalid_stream_data") }
            } finally {
                stream.close()
            }
        }
    }
}