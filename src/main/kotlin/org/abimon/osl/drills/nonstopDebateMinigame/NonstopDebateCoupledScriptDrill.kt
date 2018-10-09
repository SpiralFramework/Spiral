package org.abimon.osl.drills.nonstopDebateMinigame

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.pushStaticDrill
import org.abimon.osl.pushStaticDrillDirect
import org.abimon.spiral.core.utils.and
import org.parboiled.Action
import org.parboiled.Rule

object NonstopDebateCoupledScriptDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "Coupled Script",
                    FirstOf(
                            Sequence(
                                    ":",
                                    OptionalWhitespace(),
                                    FirstOf(
                                            Sequence(
                                                    "null",
                                                    pushStaticDrill(OSLVariable.NonstopDebateCoupledScript(null))
                                            ),
                                            Sequence(
                                                    NTimes(3, RuleWithVariables(OneOrMore(Digit())), CommaSeparator()),
                                                    Action<Any> {
                                                        val c = pop().toString().toIntOrNull()
                                                        val b = pop().toString().toIntOrNull()
                                                        val a = pop().toString().toIntOrNull()

                                                        if (a == null || b == null || c == null)
                                                            pushStaticDrillDirect(OSLVariable.NonstopDebateCoupledScript(null))
                                                        else
                                                            pushStaticDrillDirect(OSLVariable.NonstopDebateCoupledScript(a to b and c))
                                                    }
                                            )
                                    )
                            ),
                            Sequence(
                                    OptionalWhitespace(),
                                    '(',
                                    OptionalWhitespace(),
                                    FirstOf(
                                            Sequence(
                                                    "null",
                                                    pushStaticDrill(OSLVariable.NonstopDebateCoupledScript(null))
                                            ),
                                            Sequence(
                                                    NTimes(3, RuleWithVariables(OneOrMore(Digit())), CommaSeparator()),
                                                    Action<Any> {
                                                        val c = pop().toString().toIntOrNull()
                                                        val b = pop().toString().toIntOrNull()
                                                        val a = pop().toString().toIntOrNull()

                                                        if (a == null || b == null || c == null)
                                                            pushStaticDrillDirect(OSLVariable.NonstopDebateCoupledScript(null))
                                                        else
                                                            pushStaticDrillDirect(OSLVariable.NonstopDebateCoupledScript(a to b and c))
                                                    }
                                            )
                                    ),
                                    OptionalWhitespace(),
                                    ')'
                            )
                    )
            )
}