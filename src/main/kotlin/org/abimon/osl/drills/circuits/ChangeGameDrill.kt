package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.UDG
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.parboiled.Action
import org.parboiled.Rule

object ChangeGameDrill : DrillCircuit {
    val cmd = "CHANGE-GAME"
    val games = HashMap<String, DRGame>().apply {
        DR1.names.forEach { name -> put(name.toUpperCase(), DR1) }
        DR2.names.forEach { name -> put(name.toUpperCase(), DR2) }
        UDG.names.forEach { name -> put(name.toUpperCase(), UDG) }

        V3.names.forEach { name -> put(name.toUpperCase(), V3) }

        UnknownHopesPeakGame.names.forEach { name -> put(name.toUpperCase(), UnknownHopesPeakGame) }
    }

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("Game:", "Game Is ", "Set Game To "),
                    pushTmpAction(cmd, this@ChangeGameDrill),
                    OptionalWhitespace(),
                    Parameter(cmd),
                    Action<Any> { tmpStack[cmd]?.peek()?.toString()?.toUpperCase() in games },
                    operateOnTmpActions(cmd) { stack -> operate(this, stack.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        parser.game = games[rawParams[0].toString().toUpperCase()] ?: UnknownHopesPeakGame
    }
}