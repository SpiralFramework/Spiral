package info.spiralframework.osl.drills.nonstopDebateMinigame

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.data.nonstopDebate.OSLVariable
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.osl.pushStaticDrill
import info.spiralframework.osl.pushStaticDrillDirect
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

object CorrectEvidenceDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val evidenceID = Var<Int>(0)

        return Sequence(
                Action<Any> { evidenceID.set(0) },
                "Correct Evidence",
                FirstOf(
                        Sequence(
                                ":",
                                OptionalWhitespace(),
                                EvidenceID(),
                                Action<Any> { evidenceID.set(pop().toString().toIntOrNull() ?: 0) }
                        ),
                        Sequence(
                                OptionalWhitespace(),
                                '(',
                                OptionalWhitespace(),
                                EvidenceID(),
                                Action<Any> { evidenceID.set(pop().toString().toIntOrNull() ?: 0) },
                                OptionalWhitespace(),
                                ')'
                        )
                ),
                Action<Any> { pushStaticDrillDirect(OSLVariable.NonstopCorrectEvidence(evidenceID.get())) },
                pushStaticDrill(OSLVariable.NonstopChangeOperatingBlock(OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_SUCCESS)),
                Optional(
                        OptionalWhitespace(),
                        "{",
                        '\n',
                        OpenSpiralLines(),
                        '}'
                ),
                pushStaticDrill(OSLVariable.NonstopChangeOperatingBlock(OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_NONE))
        )
    }
}
