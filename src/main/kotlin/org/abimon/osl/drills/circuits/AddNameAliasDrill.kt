package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.parboiled.Rule

object AddNameAliasDrill : DrillCircuit {
    val cmd = "ADD-NAME-ALIAS"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Add alias",
                    Whitespace(),
                    pushTmpAction(cmd, this@AddNameAliasDrill),
                    Parameter(cmd),
                    Whitespace(),
                    "to",
                    Whitespace(),
                    FirstOf(
                            Parameter(cmd),
                            Sequence(
                                    Digit(),
                                    pushTmpAction(cmd)
                            )
                    ),
                    operateOnTmpActions(cmd) { params -> operate(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        val id: Int
        when (rawParams[1]) {
            is Int -> id = rawParams[2] as Int
            else -> {
                if(parser.game is HopesPeakDRGame)
                    id = (parser.game as? HopesPeakDRGame ?: UnknownHopesPeakGame).characterIdentifiers[rawParams[1].toString()] ?: 0
                else
                    id = 0
            }
        }

        parser.customIdentifiers[rawParams[0].toString()] = id
    }
}