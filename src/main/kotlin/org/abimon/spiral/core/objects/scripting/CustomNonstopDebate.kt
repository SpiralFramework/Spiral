package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.ICompilable
import org.abimon.spiral.core.objects.game.hpa.HopesPeakKillingGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.utils.writeInt16LE
import java.io.OutputStream

class CustomNonstopDebate: ICompilable {
    override val dataSize: Long
        get() = 4L + (sections.size * bytesPerSection)

    val sections: MutableList<NonstopDebateSection> = ArrayList()
    var bytesPerSection: Int = 0
    var game: HopesPeakKillingGame = UnknownHopesPeakGame
        set(value) {
            field = value
            bytesPerSection = value.nonstopDebateSectionSize
        }

    var timeLimit = 300

    fun section(section: NonstopDebateSection) { sections.add(section) }

    override fun compile(output: OutputStream) {
        output.writeInt16LE(timeLimit)
        output.writeInt16LE(sections.size)

        sections.forEach { section ->
            section.data.forEachIndexed { index, num ->
                if(index * 2 <= bytesPerSection)
                    output.writeInt16LE(num.toLong())
            }
        }
    }
}