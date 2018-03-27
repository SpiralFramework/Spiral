package org.abimon.osl.drills.circuits

import org.abimon.osl.*
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.*
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Action
import org.parboiled.Rule

object ChangeGameDrill : DrillHead {
    val cmd = "CHANGE-GAME"
    val games = mapOf(
            "DR1" to DR1,
            "DR2" to DR2,
            "SDR2" to DR2,
            "UDG" to UDG,
            "DRAE" to UDG,
            "AE" to UDG,
            "UNK" to UnknownHopesPeakGame
    )

    override fun Syntax(parser: OpenSpiralLanguageParser): Rule = parser.makeCommand {
        Sequence(
                FirstOf("Game:", "Game Is ", "Set Game To "),
                pushTmpAction(this, cmd, this@ChangeGameDrill),
                ZeroOrMore(Whitespace()),
                OneOrMore(LineMatcher),
                Action<Any> { match().toUpperCase() in games },
                pushTmpAction(this, cmd),
                operateOnTmpStack(this, cmd) { gameName -> game = games[gameName.toString().toUpperCase()] ?: return@operateOnTmpStack },
                pushTmpStack(this, cmd)
        )
    }

    override fun formScripts(rawParams: Array<Any>, game: HopesPeakDRGame): Array<LinScript> = emptyArray()

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        parser.game = games[rawParams[0].toString().toUpperCase()] ?: UnknownHopesPeakGame
    }
}