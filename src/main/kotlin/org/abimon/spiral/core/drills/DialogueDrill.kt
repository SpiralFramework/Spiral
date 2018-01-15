package org.abimon.spiral.core.drills

import org.abimon.spiral.core.SpiralConfig
import org.abimon.visi.collections.get
import org.abimon.spiral.core.lin.*
import org.abimon.spiral.core.lin.WaitForInputEntry
import org.abimon.spiral.core.lin.dr1.WaitFrameEntry
import org.abimon.spiral.util.*
import org.parboiled.BaseParser
import org.parboiled.Rule

object DialogueDrill: DrillHead {
    val cmd = "DIALOGUE"

    override fun Syntax(parser: BaseParser<Any>): Rule = parser.makeCommand {
        Sequence(
                clearTmpStack(cmd),
                OneOrMore(AllButMatcher(charArrayOf(':', '\n'))),
                pushTmpAction(this, cmd, this@DialogueDrill),
                pushTmpAction(this, cmd),
                ':',
                OneOrMore(LineMatcher),
                pushTmpAction(this, cmd),
                pushTmpStack(this, cmd)
        )
    }

    override fun formScripts(rawParams: Array<Any>, config: SpiralConfig): Array<LinScript> {
        return arrayOf(
                SpeakerEntry(config.characters["${rawParams[0]}"] ?: config.characters["???"] ?: 0), //Replace with valid naming or numbers or whatever
                TextEntry("${rawParams[1]}"),
                WaitFrameEntry(),
                WaitFrameEntry(),
                WaitForInputEntry()
        )
    }
}