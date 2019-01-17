package info.spiralframework.osl.drills.nonstopDebateMinigame

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.data.nonstopDebate.OSLVariable
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.osl.pushStaticDrill
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
