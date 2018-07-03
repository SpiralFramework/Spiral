package org.abimon.osl.drills.circuits

import org.abimon.osl.GameContext
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.parboiled.Rule

object AddNameAliasDrill : DrillCircuit {
    val cmd = "ADD-NAME-ALIAS"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            "Add alias",
                            InlineWhitespace(),
                            pushDrillHead(cmd, this@AddNameAliasDrill),
                            Parameter(cmd),
                            InlineWhitespace(),
                            "to",
                            InlineWhitespace(),
                            FirstOf(
                                    Parameter(cmd),
                                    Sequence(
                                            Digit(),
                                            pushTmpAction(cmd)
                                    )
                            ),
                            operateOnTmpActions(cmd) { params -> operate(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        val id: Int
        when (rawParams[1]) {
            is Int -> id = rawParams[1] as Int
            else -> {
                val intID = (rawParams[1].toString()).toIntOrNull()
                if(intID != null)
                    id = intID
                else if (parser.gameContext is GameContext.HopesPeakGameContext)
                    id = (parser.hopesPeakGame ?: UnknownHopesPeakGame).characterIdentifiers[rawParams[1].toString()] ?: 0
                else if (parser.gameContext is GameContext.V3GameContext)
                    id = (parser.v3Game ?: V3).characterIdentifiers[rawParams[1].toString()] ?: 0
                else
                    id = 0
            }
        }

        parser.customIdentifiers[rawParams[0].toString()] = id
    }
}