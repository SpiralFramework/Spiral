package org.abimon.osl.drills.nonstopDebateMinigame

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.pushStaticDrill
import org.abimon.osl.pushStaticDrillDirect
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

object NonstopDebateOperatingDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val otherID = Var<Int>(0)

        return Sequence(
                FirstOf(
                        Sequence(
                                FirstOf(
                                        "Trial Loop",
                                        "On Trial Loop",
                                        "On Loop",
                                        "When Looping"
                                ),
                                pushStaticDrill(OSLVariable.NonstopChangeStage(OSLVariable.VALUES.NONSTOP_STAGE_POST_TEXT)),
                                pushStaticDrill(UnknownEntry(0x2E, intArrayOf((40000) shr 8, (40000) and 0xFF)))
                        ),
                        Sequence(
                                FirstOf(
                                        "Out of Health",
                                        "On Out of Health",
                                        "On Death",
                                        "When Out of Health",
                                        "When Dead"
                                ),
                                pushStaticDrill(OSLVariable.NonstopChangeStage(OSLVariable.VALUES.NONSTOP_STAGE_POST_TEXT)),
                                pushStaticDrill(UnknownEntry(0x2E, intArrayOf((50000) shr 8, (50000) and 0xFF)))
                        ),
                        Sequence(
                                FirstOf(
                                        "Out of Time",
                                        "On Out of Time",
                                        "On Timeout",
                                        "When Out of Time"
                                ),
                                pushStaticDrill(OSLVariable.NonstopChangeStage(OSLVariable.VALUES.NONSTOP_STAGE_POST_TEXT)),
                                pushStaticDrill(UnknownEntry(0x2E, intArrayOf((60000) shr 8, (60000) and 0xFF)))
                        ),
                        Sequence(
                                "On Other Event",
                                FirstOf(
                                        Sequence(
                                                ":",
                                                OptionalWhitespace(),
                                                RuleWithVariables(OneOrMore(Digit())),
                                                Action<Any> { otherID.set(pop().toString().toIntOrNull() ?: 0) }
                                        ),
                                        Sequence(
                                                OptionalWhitespace(),
                                                '(',
                                                OptionalWhitespace(),
                                                RuleWithVariables(OneOrMore(Digit())),
                                                Action<Any> { otherID.set(pop().toString().toIntOrNull() ?: 0) },
                                                OptionalWhitespace(),
                                                ')'
                                        )
                                ),
                                pushStaticDrill(OSLVariable.NonstopChangeStage(OSLVariable.VALUES.NONSTOP_STAGE_POST_TEXT)),
                                Action<Any> { pushStaticDrillDirect(UnknownEntry(0x2E, intArrayOf(otherID.get() shr 8, otherID.get() and 0xFF))) }
                        )
                ),
                OptionalWhitespace(),
                "{",
                '\n',
                OpenSpiralLines(),
                "}"
        )
    }
}