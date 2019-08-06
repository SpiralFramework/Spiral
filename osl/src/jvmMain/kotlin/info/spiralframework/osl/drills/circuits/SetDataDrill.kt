package info.spiralframework.osl.drills.circuits

import info.spiralframework.osl.OpenSpiralLanguageParser
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
