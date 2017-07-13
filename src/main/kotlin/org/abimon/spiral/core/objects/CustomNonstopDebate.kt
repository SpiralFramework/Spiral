package org.abimon.spiral.core.objects

import org.abimon.spiral.core.writeNumber
import java.io.OutputStream

class CustomNonstopDebate {
    val sections = ArrayList<NonstopSection>()
    var bytesPerSection = 60
    var secondsForDebate = 600

    fun section(section: NonstopSection) = sections.add(section)

    fun compile(output: OutputStream) {
        output.writeNumber(secondsForDebate / 2L, 2, unsigned = true)
        output.writeNumber(sections.size.toLong(), 2, unsigned = true)

        sections.forEach { it.data.forEachIndexed { index, num -> if(index * 2 <= bytesPerSection) output.writeNumber(num.toLong(), 2, unsigned = true) } }
    }
}