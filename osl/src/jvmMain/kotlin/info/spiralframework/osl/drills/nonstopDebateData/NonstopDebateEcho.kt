package info.spiralframework.osl.drills.nonstopDebateData

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.circuits.DrillCircuit
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
