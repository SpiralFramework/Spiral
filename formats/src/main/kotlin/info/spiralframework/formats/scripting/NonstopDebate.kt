package info.spiralframework.formats.scripting

import info.spiralframework.base.assertAsLocaleArgument
import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.foldToInt16LE
import info.spiralframework.formats.utils.readInt16LE
import java.io.InputStream

class NonstopDebate private constructor(val game: HopesPeakKillingGame, val dataSource: () -> InputStream) {
    companion object {
        operator fun invoke(game: HopesPeakKillingGame, dataSource: () -> InputStream): NonstopDebate? {
            try {
                return NonstopDebate(game, dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.nonstop.invalid", dataSource, game, iae)

                return null
            }
        }

        fun unsafe(game: HopesPeakKillingGame, dataSource: () -> InputStream): NonstopDebate = NonstopDebate(game, dataSource)
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
        val stream = dataSource()

        try {
            //First up, we have to read the time limit and number of sections, as Int16LE variables
            timeLimit = stream.readInt16LE()
            numberOfSections = stream.readInt16LE()

            gentleTimeLimit = timeLimit * 2
            meanTimeLimit   = (timeLimit * 0.8).toInt()

            val sectionBuffer = ByteArray(game.nonstopDebateSectionSize)

            sections = Array(numberOfSections) {
                val read = stream.read(sectionBuffer)
                assertAsLocaleArgument(read == sectionBuffer.size, "formats.nonstop.invalid_stream_size", read, sectionBuffer.size)

                return@Array NonstopDebateSection(sectionBuffer.foldToInt16LE())
            }

            assertAsLocaleArgument(stream.read() == -1, "formats.nonstop.invalid_stream_data")
        } finally {
            stream.close()
        }
    }
}