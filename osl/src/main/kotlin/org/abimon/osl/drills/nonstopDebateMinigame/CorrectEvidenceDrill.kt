package org.abimon.osl.drills.nonstopDebateMinigame

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.pushStaticDrill
import org.abimon.osl.pushStaticDrillDirect
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