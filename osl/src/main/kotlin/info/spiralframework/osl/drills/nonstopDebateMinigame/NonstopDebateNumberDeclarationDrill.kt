package info.spiralframework.osl.drills.nonstopDebateMinigame

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.data.nonstopDebate.OSLVariable
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.osl.pushStaticDrill
import info.spiralframework.osl.pushStaticDrillDirect
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
