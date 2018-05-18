package org.abimon.osl.drills.circuits

import org.abimon.osl.LineMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Action
import org.parboiled.Rule

object MacroDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    OneOrMore(LineMatcher),
                    Action<Any> {
                        val macros = (data["macros"] as? Map<*, *>
                                ?: return@Action false).mapKeys { (key) -> key?.toString()?.toLowerCase() ?: "" }
                                .mapValues { (_, value) -> value as? List<*> ?: emptyList<Any>() }

                        val macro = macros[match().toLowerCase()]?.filterNotNull() ?: return@Action false

                        macro.forEach(this::pushValue)

                        return@Action true
                    }
            )
}