package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.writeShort
import java.io.OutputStream

class CustomNonstopDebate {
    val sections = ArrayList<NonstopSection>()
    var bytesPerSection = 60
    var secondsForDebate = 600

    fun section(section: NonstopSection) = sections.add(section)

    fun compile(output: OutputStream) {
        output.writeShort(secondsForDebate / 2L)
        output.writeShort(sections.size.toLong())

        sections.forEach { it.data.forEachIndexed { index, num -> if(index * 2 <= bytesPerSection) output.writeShort(num.toLong()) } }
    }
}