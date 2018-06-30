package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.ICompilable
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.utils.writeInt16LE
import java.io.OutputStream

class CustomNonstopDebate {
//    override val dataSize: Long
//        get() = 4 + (sections.size * )
//    val sections: MutableList = ArrayList<NonstopDebate>()
//    var game: HopesPeakDRGame = UnknownHopesPeakGame
//        set(value) {
//            field = value
//
//            when (field) {
//
//            }
//        }
//    var timeLimit = 300
//
//    private var bytesPerSection: Int = 0
//
//    fun section(section: NonstopDebateSection) = sections.add(section)
//
//    fun compile(output: OutputStream) {
//        output.writeInt16LE(timeLimit)
//        output.writeInt16LE(sections.size)
//
//        sections.forEach { it.data.forEachIndexed { index, num -> if(index * 2 <= bytesPerSection) output.writeShort(num.toLong()) } }
//    }
}