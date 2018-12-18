package info.spiralframework.formats.scripting

import info.spiralframework.formats.ICompilable
import info.spiralframework.formats.game.hpa.HopesPeakKillingGame
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import info.spiralframework.formats.utils.writeInt16LE
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

    var currentSection: NonstopDebateSection? = null
        set(value) {
            field?.let(this::section)
            field = value
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