package info.spiralframework.osl.drills.nonstopDebateMinigame

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.data.nonstopDebate.OSLVariable
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.osl.pushStaticDrill
import org.parboiled.Rule

object NonstopDebateTextEntryDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    FirstOf(
                            "Raw Debate Text",
                            "Raw Debate Entries"
                    ),
                    pushStaticDrill(OSLVariable.NonstopChangeOperatingBlock(OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_TEXT)),
                    OptionalWhitespace(),
                    "{",
                    '\n',
                    OpenSpiralLines(),
                    "}",
                    pushStaticDrill(OSLVariable.NonstopChangeOperatingBlock(OSLVariable.VALUES.NONSTOP_OPERATING_BLOCK_NONE))
            )
}
