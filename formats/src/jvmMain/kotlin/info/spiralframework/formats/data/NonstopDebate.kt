package info.spiralframework.formats.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.utils.foldToInt16LE
import java.io.InputStream

class NonstopDebate private constructor(context: SpiralContext, val game: HopesPeakKillingGame, val dataSource: () -> InputStream) {
    companion object {
        operator fun invoke(context: SpiralContext, game: HopesPeakKillingGame, dataSource: () -> InputStream): NonstopDebate? {
            withFormats(context) {
                try {
                    return NonstopDebate(this, game, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.nonstop.invalid", dataSource, game, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, game: HopesPeakKillingGame, dataSource: () -> InputStream): NonstopDebate = withFormats(context) { NonstopDebate(this, game, dataSource) }
    }

    /** Time limit in seconds */
    val timeLimit: Int

    /** 2 * timeLimit */
    val gentleTimeLimit: Int

    /** 0.8 * timeLimit */
    val meanTimeLimit: Int

    /** Can also be read from the array length but for now... */
    val numberOfSections: Int

    val sections: Array<NonstopDebateSection>

    init {
        with(context) {
            val stream = dataSource()

            try {
                //First up, we have to read the time limit and number of sections, as Int16LE variables
                timeLimit = stream.readInt16LE()
                numberOfSections = stream.readInt16LE()

                gentleTimeLimit = timeLimit * 2
                meanTimeLimit = (timeLimit * 0.8).toInt()

                val sectionBuffer = ByteArray(game.nonstopDebateSectionSize)

                sections = Array(numberOfSections) {
                    val read = stream.read(sectionBuffer)
                    require(read == sectionBuffer.size) { localise("formats.nonstop.invalid_stream_size", read, sectionBuffer.size) }

                    return@Array NonstopDebateSection(sectionBuffer.foldToInt16LE())
                }

                require(stream.read() == -1) { localise("formats.nonstop.invalid_stream_data") }
            } finally {
                stream.close()
            }
        }
    }
}