package org.abimon.osl.drills

import org.abimon.osl.*
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.parboiled.Rule

object BasicTextDrill : DrillHead {
    val cmd = "BASIC_TEXT"

    override fun Syntax(parser: OpenSpiralLanguageParser): Rule = parser.makeCommand {
        Sequence(
                clearTmpStack(cmd),
                FirstOf(
                        Sequence(
                                "0x",
                                Optional("0"),
                                "2"
                        ),
                        IgnoreCase("Text")
                ),
                '|',
                pushTmpAction(this, cmd, this@BasicTextDrill),
                OneOrMore(LineCodeMatcher),
                pushTmpAction(this, cmd),
                pushTmpStack(this, cmd)
        )
    }

    override fun formScripts(rawParams: Array<Any>, game: HopesPeakDRGame): Array<LinScript> = arrayOf(TextEntry("${rawParams[0]}", -1))
}