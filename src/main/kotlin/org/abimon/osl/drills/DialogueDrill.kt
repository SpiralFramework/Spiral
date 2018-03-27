package org.abimon.osl.drills

import org.abimon.osl.*
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.*
import org.parboiled.Action
import org.parboiled.Rule

object DialogueDrill : DrillHead {
    val NAME = AllButMatcher(charArrayOf(':', '\n'))
    val cmd = "DIALOGUE"

    override fun Syntax(parser: OpenSpiralLanguageParser): Rule = parser.makeCommand {
        Sequence(
                clearTmpStack(cmd),
                OneOrMore(NAME),
                Action<Any> { match() in game.characterIdentifiers },
                pushTmpAction(this, cmd, this@DialogueDrill),
                pushTmpAction(this, cmd),
                ':',
                OneOrMore(LineMatcher),
                pushTmpAction(this, cmd),
                pushTmpStack(this, cmd)
        )
    }

    override fun formScripts(rawParams: Array<Any>, game: HopesPeakDRGame): Array<LinScript> {
        return arrayOf(
                SpeakerEntry(game.characterIdentifiers["${rawParams[0]}"] ?: game.characterIdentifiers["???"] ?: 0), //Replace with valid naming or numbers or whatever
                TextEntry("${rawParams[1]}", -1),
                game.run {
                    when (this) {
                        DR1 -> WaitFrameEntry.DR1
                        DR2 -> WaitFrameEntry.DR2
                        else -> TODO("Unknown game $game (Text hasn't been completely documented!)")
                    }
                },
                game.run {
                    when (this) {
                        DR1 -> WaitForInputEntry.DR1
                        DR2 -> WaitForInputEntry.DR2
                        else -> TODO("Unknown game $game (Text hasn't been completely documented!)")
                    }
                }
        )
    }
}