package org.abimon.osl.drills.nonstopDebate

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.data.nonstopDebate.NonstopDebateNewObject
import org.abimon.osl.drills.StaticDrill
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.parboiled.Action
import org.parboiled.Rule

object NonstopDebateNewObjectDrill: DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    FirstOf(
                            "New Nonstop Section",
                            "[New Section]",
                            "[New Nonstop Debate Section]"
                    ),

                    Action<Any> {
                        push(listOf(SpiralDrillBit(StaticDrill(NonstopDebateNewObject((nonstopDebateGame ?: UnknownHopesPeakGame).nonstopDebateSectionSize / 2)))))
                    }
            )
}