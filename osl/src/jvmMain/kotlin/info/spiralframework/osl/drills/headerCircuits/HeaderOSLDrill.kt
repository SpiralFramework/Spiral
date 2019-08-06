package info.spiralframework.osl.drills.headerCircuits

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule

object HeaderOSLDrill: DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    FirstOf(
                            Sequence(
                                    "Header:",
                                    OptionalInlineWhitespace()
                            ),
                            Sequence(
                                    "Load Header",
                                    InlineWhitespace()
                            ),
                            Sequence(
                                    "Import",
                                    FirstOf(
                                            Sequence(":", OptionalInlineWhitespace()),
                                            InlineWhitespace()
                                    )
                            )
                    ),
                    ParameterToStack(),
                    Action<Any> { push(arrayOf(this, String(load(pop().toString()) ?: return@Action false))) }
            )
}
