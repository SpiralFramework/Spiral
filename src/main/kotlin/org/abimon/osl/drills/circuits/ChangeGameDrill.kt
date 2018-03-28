package org.abimon.osl.drills.circuits

import org.abimon.osl.LineMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.UDG
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.parboiled.Action
import org.parboiled.Rule

object ChangeGameDrill : DrillCircuit {
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

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("Game:", "Game Is ", "Set Game To "),
                    pushTmpAction(cmd, this@ChangeGameDrill),
                    ZeroOrMore(Whitespace()),
                    OneOrMore(LineMatcher),
                    Action<Any> { match().toUpperCase() in games },
                    pushTmpAction(cmd),
                    operateOnTmpActions(cmd) { stack -> operate(this, stack.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        parser.game = games[rawParams[0].toString().toUpperCase()] ?: UnknownHopesPeakGame
    }
}