package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule
import java.lang.Math.abs

object ForLoopDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "for",
                    OptionalWhitespace(),
                    "(",
                    FirstOf(
                            Sequence(
                                    ParameterToStack(),
                                    Whitespace(),
                                    "in",
                                    Whitespace(),
                                    FirstOf(
                                            Sequence(
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    Whitespace(),
                                                    FirstOf("until", "downTo", "down_to"),
                                                    Whitespace(),
                                                    RuleWithVariables(OneOrMore(Digit()))
                                            ),
                                            Sequence(
                                                    "range",
                                                    OptionalWhitespace(),
                                                    '(',
                                                    OptionalWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    OptionalWhitespace(),
                                                    ',',
                                                    OptionalWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    OptionalWhitespace(),
                                                    ')'
                                            )
                                    )
                            ),
                            Sequence(
                                    Optional("int", OptionalWhitespace()),
                                    ParameterToStack(),
                                    OptionalWhitespace(),
                                    "=",
                                    OptionalWhitespace(),
                                    RuleWithVariables(OneOrMore(Digit())),
                                    OptionalWhitespace(),
                                    ';',
                                    OptionalWhitespace(),
                                    ParameterToStack(),
                                    Action<Any> {
                                        val current = pop()
                                        val original = peek(1)

                                        return@Action current == original
                                    },
                                    OptionalWhitespace(),
                                    FirstOf("<", ">"),
                                    OptionalWhitespace(),
                                    RuleWithVariables(OneOrMore(Digit())),
                                    OptionalWhitespace(),
                                    Optional(
                                            ';',
                                            OptionalWhitespace(),
                                            ParameterToStack(),
                                            Action<Any> {
                                                val current = pop()
                                                val original = peek(2)

                                                return@Action current == original
                                            },
                                            OptionalWhitespace(),
                                            FirstOf(
                                                    "++",
                                                    "--",
                                                    Sequence(
                                                            "+=",
                                                            OptionalWhitespace(),
                                                            RuleWithVariables(OneOrMore(Digit()))
                                                    ),
                                                    Sequence(
                                                            "-=",
                                                            OptionalWhitespace(),
                                                            RuleWithVariables(OneOrMore(Digit()))
                                                    )
                                            )
                                    )
                            )
                    ),
                    OptionalWhitespace(),
                    ')',
                    OptionalWhitespace(),
                    '{',
                    '\n',
                    Action<Any> {
                        this[peek(2).toString()] = 0

                        return@Action true
                    },
                    Sequence(
                            saveState(),
                            OpenSpiralLines(),
                            Action<Any> { context ->
                                loadState(context)

                                var limit = pop().toString().toInt()
                                val start = pop().toString().toInt()
                                val variableName = pop().toString()

                                val range = abs(limit - start)

                                if (range > maxForLoopFange) {
                                    if (start < limit)
                                        limit = start + maxForLoopFange
                                    else
                                        limit = start - maxForLoopFange
                                }

                                for (i in (if (start < limit) start until limit else start downTo limit)) {
                                    val data = context.match
                                    push(arrayOf(this, "Set Variable \"$variableName\" to \"$i\""))
                                    push(arrayOf(this, data))
                                }

                                return@Action true
                            }
                    ),
                    "}"
            )
}