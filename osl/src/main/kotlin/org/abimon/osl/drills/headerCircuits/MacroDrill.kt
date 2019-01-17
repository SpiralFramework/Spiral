package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.LineMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
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