package org.abimon.osl.drills.nonstopDebateMinigame

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.pushStaticDrill
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

object IncorrectEvidenceDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val evidenceID = Var<Int>(0)

        return Sequence(
                Action<Any> { evidenceID.set(0) },
                FirstOf(
                        "Incorrect Evidence",
                        "On Incorrect Evidence",
                        "On Incorrect",
                        "When Wrong",
                        "On Wrong",
                        "On Fail"
                ),
                OptionalWhitespace(),
                "{",
                '\n',
                pushStaticDrill(OSLVariable.NonstopChangeOperatingBlock(OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_FAIL)),
                OpenSpiralLines(),
                "}",
                pushStaticDrill(OSLVariable.NonstopChangeOperatingBlock(OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_NONE))
        )
    }
}