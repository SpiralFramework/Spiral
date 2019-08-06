package info.spiralframework.osl.drills.headerCircuits

import info.spiralframework.osl.LineMatcher
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule

object MacroDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    OneOrMore(LineMatcher),
                    Action<Any> {
                        val macro = macros[match().toLowerCase()] ?: return@Action false
                        push(arrayOf(this, macro))
                        return@Action true
                    }
            )
}
