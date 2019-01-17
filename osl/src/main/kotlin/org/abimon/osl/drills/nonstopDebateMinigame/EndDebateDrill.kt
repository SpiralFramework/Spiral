package org.abimon.osl.drills.nonstopDebateMinigame

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.pushStaticDrill
import org.parboiled.Rule

object EndDebateDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    FirstOf(
                            "End Nonstop Debate",
                            "End Debate",
                            "Finish Debate",
                            "[End Nonstop Debate]",
                            "[End Debate]",
                            "[Finish Debate]"
                    ),

                    pushStaticDrill(OSLVariable.NonstopChangeStage(OSLVariable.VALUES.NONSTOP_STAGE_POST_SCRIPT))
            )
}