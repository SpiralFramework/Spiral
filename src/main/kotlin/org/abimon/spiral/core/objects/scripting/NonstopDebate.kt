package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.HopesPeakKillingGame
import org.abimon.spiral.core.utils.assertAsArgument
import org.abimon.spiral.core.utils.foldToInt16LE
import org.abimon.spiral.core.utils.readInt16LE
import java.io.InputStream

class NonstopDebate private constructor(val game: HopesPeakKillingGame, val dataSource: () -> InputStream) {
    companion object {
        operator fun invoke(game: HopesPeakDRGame, dataSource: () -> InputStream): NonstopDebate? {
            try {
                return NonstopDebate(game, dataSource)
            } catch (iae: IllegalArgumentException) {
                return null
            }
        }
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