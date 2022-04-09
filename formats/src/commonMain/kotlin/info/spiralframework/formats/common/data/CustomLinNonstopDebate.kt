package info.spiralframework.formats.common.data

import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.formats.common.games.Dr1
import info.spiralframework.formats.common.games.Dr2
import info.spiralframework.formats.common.games.DrGame
import kotlin.math.roundToInt

public class CustomLinNonstopDebate(public val sectionSize: Int) {
    public constructor(game: DrGame.LinNonstopScriptable) : this(game.linNonstopSectionSize)

    public val _sections: MutableList<LinNonstopDebateSection> = ArrayList()
    public val sections: List<LinNonstopDebateSection>
        get() = _sections

    public var baseTimeLimit: Int = 300

    /** 2 * timeLimit */
    public var gentleTimeLimit: Int
        get() = baseTimeLimit * 2
        set(value) {
            baseTimeLimit = value / 2
        }

    /** 1 * timeLimit */
    public var kindTimeLimit: Int
        get() = baseTimeLimit * 1
        set(value) {
            baseTimeLimit = value / 1
        }

    /** 0.8 * timeLimit */
    public var meanTimeLimit: Int
        get() = (baseTimeLimit * 0.8).roundToInt()
        set(value) {
            baseTimeLimit = (value / 0.8).roundToInt()
        }

    public inline fun addSection(block: LinNonstopDebateSection.() -> Unit) {
        val section = LinNonstopDebateSection(sectionSize)
        section.block()
        _sections.add(section)
    }

    public fun addSection(section: LinNonstopDebateSection) {
        _sections.add(section)
    }

    public suspend fun compile(output: OutputFlow) {
        output.writeInt16LE(baseTimeLimit)
        output.writeInt16LE(_sections.size)

        _sections.forEach { section ->
            section.data
                    .copyOfRange(0, sectionSize)
                    .suspendForEach(output::writeInt16LE)
        }
    }
}

public inline fun dr1NonstopDebate(block: CustomLinNonstopDebate.() -> Unit): CustomLinNonstopDebate = linNonstopDebate(Dr1.NONSTOP_DEBATE_SECTION_SIZE, block)
public inline fun dr2NonstopDebate(block: CustomLinNonstopDebate.() -> Unit): CustomLinNonstopDebate = linNonstopDebate(Dr2.NONSTOP_DEBATE_SECTION_SIZE, block)

public suspend fun OutputFlow.compileDr1NonstopDebate(block: CustomLinNonstopDebate.() -> Unit): Unit = compileLinNonstopDebate(Dr1.NONSTOP_DEBATE_SECTION_SIZE, block)
public suspend fun OutputFlow.compileDr2NonstopDebate(block: CustomLinNonstopDebate.() -> Unit): Unit = compileLinNonstopDebate(Dr2.NONSTOP_DEBATE_SECTION_SIZE, block)

public inline fun linNonstopDebate(game: DrGame.LinNonstopScriptable, block: CustomLinNonstopDebate.() -> Unit): CustomLinNonstopDebate = linNonstopDebate(game.linNonstopSectionSize, block)
public inline fun linNonstopDebate(sectionSize: Int, block: CustomLinNonstopDebate.() -> Unit): CustomLinNonstopDebate {
    val debate = CustomLinNonstopDebate(sectionSize)
    debate.block()
    return debate
}

public suspend fun OutputFlow.compileLinNonstopDebate(game: DrGame.LinNonstopScriptable, block: CustomLinNonstopDebate.() -> Unit): Unit = compileLinNonstopDebate(game.linNonstopSectionSize, block)
public suspend fun OutputFlow.compileLinNonstopDebate(sectionSize: Int, block: CustomLinNonstopDebate.() -> Unit) {
    val debate = CustomLinNonstopDebate(sectionSize)
    debate.block()
    debate.compile(this)
}