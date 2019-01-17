package org.abimon.osl.drills.nonstopDebateMinigame

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.pushStaticDrill
import org.abimon.osl.pushStaticDrillDirect
import org.parboiled.Action
import org.parboiled.Rule

object NonstopDebateNumberDeclarationDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "Nonstop Debate",
                    InlineWhitespace(),
                    FirstOf(
                            "#",
                            "No",
                            "Number"
                    ),
                    RuleWithVariables(OneOrMore(Digit())),
                    pushStaticDrill(OSLVariable.NonstopChangeStage(OSLVariable.VALUES.NONSTOP_STAGE_PRE_TEXT)),
                    Action<Any> { pushStaticDrillDirect(OSLVariable.NonstopDebateNumber(pop(1).toString().toIntOrNull() ?: 0)) }
            )
}