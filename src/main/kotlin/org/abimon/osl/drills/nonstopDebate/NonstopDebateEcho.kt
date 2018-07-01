package org.abimon.osl.drills.nonstopDebate

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule

object NonstopDebateEcho: DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "nonstop echo: ",
                    ParameterToStack(),
                    Action<Any> { println("Echo: ${pop()}"); true }
            )
}