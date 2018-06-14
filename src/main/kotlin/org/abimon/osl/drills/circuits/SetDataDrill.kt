package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Action
import org.parboiled.Rule

object SetDataDrill: DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "Set Variable",
                    InlineWhitespace(),
                    ParameterToStack(),
                    InlineWhitespace(),
                    "to",
                    InlineWhitespace(),
                    ParameterToStack(),

                    Action<Any> {
                        val value = pop()
                        val variable = pop()

                        data[variable.toString()] = value

                        return@Action true
                    }
            )
}