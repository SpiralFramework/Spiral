package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.pushStaticDrillDirect
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.EndFlagCheckEntry
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

object LinCheckGameStateDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val gameStateVar = Var<Int>()
        val operationVar = Var<Int>()
        val gameStateValueVar = Var<Int>()

        return Sequence(
                "Check Game State",
                Separator(),
                GameState(),
                Action<Any> { gameStateVar.set(pop().toString().toIntOrNull() ?: 0) },
                FirstOf(
                        CommaSeparator(),
                        InlineWhitespace()
                ),
                LinIfOperatorToVar(operationVar),
                FirstOf(
                        CommaSeparator(),
                        InlineWhitespace()
                ),
                GameStateValue(gameStateVar),
                Action<Any> { gameStateValueVar.set(pop().toString().toIntOrNull() ?: 0) },
                Action<Any> {
                    pushStaticDrillDirect(when (hopesPeakGame) {
                        DR1 -> arrayOf(
                                UnknownEntry(0x36, intArrayOf(0, gameStateVar.get(), operationVar.get(), gameStateValueVar.get() shr 8, gameStateValueVar.get() and 0xFF)),
                                EndFlagCheckEntry()
                        )
                        DR2 -> arrayOf(
                                UnknownEntry(0x36, intArrayOf(0, gameStateVar.get(), operationVar.get(), gameStateValueVar.get() shr 8, gameStateValueVar.get() and 0xFF)),
                                EndFlagCheckEntry()
                        )
                        else -> TODO("Flag Checks are not documented for ${hopesPeakGame}")
                    })
                }
        )
    }
}