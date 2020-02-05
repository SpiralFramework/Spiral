package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.formats.common.games.Dr1
import info.spiralframework.formats.common.games.Dr2
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.games.UnsafeDr1
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.writeInt16LE
import kotlin.math.roundToInt

class CustomLinNonstopDebate(val sectionSize: Int) {
    constructor(game: DrGame.LinNonstopScriptable) : this(game.linNonstopSectionSize)

    val _sections: MutableList<LinNonstopDebateSection> = ArrayList()
    val sections: List<LinNonstopDebateSection>
        get() = _sections

    var baseTimeLimit: Int = 300

    /** 2 * timeLimit */
    var gentleTimeLimit
        get() = baseTimeLimit * 2
        set(value) {
            baseTimeLimit = value / 2
        }

    /** 1 * timeLimit */
    var kindTimeLimit
        get() = baseTimeLimit * 1
        set(value) {
            baseTimeLimit = value / 1
        }

    /** 0.8 * timeLimit */
    var meanTimeLimit
        get() = (baseTimeLimit * 0.8).roundToInt()
        set(value) {
            baseTimeLimit = (value / 0.8).roundToInt()
        }

    inline fun addSection(block: LinNonstopDebateSection.() -> Unit) {
        val section = LinNonstopDebateSection(sectionSize)
        section.block()
        _sections.add(section)
    }

    fun addSection(section: LinNonstopDebateSection) {
        _sections.add(section)
    }

    @ExperimentalUnsignedTypes
    suspend fun compile(output: OutputFlow) {
        output.writeInt16LE(baseTimeLimit)
        output.writeInt16LE(_sections.size)

        _sections.forEach { section ->
            section.data
                    .copyOfRange(0, sectionSize)
                    .suspendForEach(output::writeInt16LE)
        }
    }
}

@ExperimentalUnsignedTypes
inline fun dr1NonstopDebate(block: CustomLinNonstopDebate.() -> Unit) = linNonstopDebate(Dr1.NONSTOP_DEBATE_SECTION_SIZE, block)

@ExperimentalUnsignedTypes
suspend fun OutputFlow.compileDr1NonstopDebate(block: CustomLinNonstopDebate.() -> Unit) = compileLinNonstopDebate(Dr1.NONSTOP_DEBATE_SECTION_SIZE, block)

@ExperimentalUnsignedTypes
inline fun dr2NonstopDebate(block: CustomLinNonstopDebate.() -> Unit) = linNonstopDebate(Dr2.NONSTOP_DEBATE_SECTION_SIZE, block)

@ExperimentalUnsignedTypes
suspend fun OutputFlow.compileDr2NonstopDebate(block: CustomLinNonstopDebate.() -> Unit) = compileLinNonstopDebate(Dr2.NONSTOP_DEBATE_SECTION_SIZE, block)

inline fun linNonstopDebate(game: DrGame.LinNonstopScriptable, block: CustomLinNonstopDebate.() -> Unit) = linNonstopDebate(game.linNonstopSectionSize, block)
inline fun linNonstopDebate(sectionSize: Int, block: CustomLinNonstopDebate.() -> Unit): CustomLinNonstopDebate {
    val debate = CustomLinNonstopDebate(sectionSize)
    debate.block()
    return debate
}

@ExperimentalUnsignedTypes
suspend fun OutputFlow.compileLinNonstopDebate(game: DrGame.LinNonstopScriptable, block: CustomLinNonstopDebate.() -> Unit) = compileLinNonstopDebate(game.linNonstopSectionSize, block)

@ExperimentalUnsignedTypes
suspend fun OutputFlow.compileLinNonstopDebate(sectionSize: Int, block: CustomLinNonstopDebate.() -> Unit) {
    val debate = CustomLinNonstopDebate(sectionSize)
    debate.block()
    debate.compile(this)
}