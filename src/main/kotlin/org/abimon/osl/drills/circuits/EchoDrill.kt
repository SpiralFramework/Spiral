package org.abimon.osl.drills.circuits

import org.abimon.osl.*
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Rule

object EchoDrill: DrillHead {
    val cmd = "ECHO"

    override fun Syntax(parser: OpenSpiralLanguageParser): Rule = parser.makeCommand {
        Sequence(
                clearTmpStack(cmd),
                "echo",
                Whitespace(),
                pushTmpAction(this, cmd, this@EchoDrill),
                OneOrMore(LineCodeMatcher),
                pushTmpAction(this, cmd),
                pushTmpStack(this, cmd)
        )
    }

    override fun formScripts(rawParams: Array<Any>, game: HopesPeakDRGame): Array<LinScript> = emptyArray()

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        println(rawParams[0])
    }
}