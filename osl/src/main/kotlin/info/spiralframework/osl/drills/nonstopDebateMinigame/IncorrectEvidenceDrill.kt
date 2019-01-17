package info.spiralframework.osl.drills.nonstopDebateMinigame

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.data.nonstopDebate.OSLVariable
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.osl.pushStaticDrill
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
