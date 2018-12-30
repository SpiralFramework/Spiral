package info.spiralframework.formats.scripting

import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.assertAsArgument
import info.spiralframework.formats.utils.foldToInt16LE
import info.spiralframework.formats.utils.readInt16LE
import java.io.InputStream

class NonstopDebate private constructor(val game: HopesPeakKillingGame, val dataSource: () -> InputStream) {
    companion object {
        operator fun invoke(game: HopesPeakKillingGame, dataSource: () -> InputStream): NonstopDebate? {
            try {
                return NonstopDebate(game, dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("Failed to compile Nonstop Debate for dataSource {} and game {}", dataSource, game, iae)

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
                assertAsArgument(read == sectionBuffer.size, "Illegal stream size for Nonstop Debate (Expected to read ${sectionBuffer.size}, instead we read $read bytes)")

                return@Array NonstopDebateSection(sectionBuffer.foldToInt16LE())
            }

            assertAsArgument(stream.read() == -1, "Illegal stream size for Nonstop Debate (More data present after sections!)")
        } finally {
            stream.close()
        }
    }
}