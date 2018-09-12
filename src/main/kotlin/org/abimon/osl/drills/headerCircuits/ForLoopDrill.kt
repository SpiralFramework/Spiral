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
                    OptionalInlineWhitespace(),
                    "(",
                    FirstOf(
                            Sequence(
                                    ParameterToStack(),
                                    InlineWhitespace(),
                                    "in",
                                    InlineWhitespace(),
                                    FirstOf(
                                            Sequence(
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    InlineWhitespace(),
                                                    FirstOf("until", "downTo", "down_to"),
                                                    InlineWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit()))
                                            ),
                                            Sequence(
                                                    "range",
                                                    OptionalInlineWhitespace(),
                                                    '(',
                                                    OptionalInlineWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    OptionalInlineWhitespace(),
                                                    ',',
                                                    OptionalInlineWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    OptionalInlineWhitespace(),
                                                    ')'
                                            )
                                    )
                            ),
                            Sequence(
                                    Optional("int", OptionalInlineWhitespace()),
                                    ParameterToStack(),
                                    OptionalInlineWhitespace(),
                                    "=",
                                    OptionalInlineWhitespace(),
                                    RuleWithVariables(OneOrMore(Digit())),
                                    OptionalInlineWhitespace(),
                                    ';',
                                    OptionalInlineWhitespace(),
                                    ParameterToStack(),
                                    Action<Any> {
                                        val current = pop()
                                        val original = peek(1)

                                        return@Action current == original
                                    },
                                    OptionalInlineWhitespace(),
                                    FirstOf("<", ">"),
                                    OptionalInlineWhitespace(),
                                    RuleWithVariables(OneOrMore(Digit())),
                                    OptionalInlineWhitespace(),
                                    Optional(
                                            ';',
                                            OptionalInlineWhitespace(),
                                            ParameterToStack(),
                                            Action<Any> {
                                                val current = pop()
                                                val original = peek(2)

                                                return@Action current == original
                                            },
                                            OptionalInlineWhitespace(),
                                            FirstOf(
                                                    "++",
                                                    "--",
                                                    Sequence(
                                                            "+=",
                                                            OptionalInlineWhitespace(),
                                                            RuleWithVariables(OneOrMore(Digit()))
                                                    ),
                                                    Sequence(
                                                            "-=",
                                                            OptionalInlineWhitespace(),
                                                            RuleWithVariables(OneOrMore(Digit()))
                                                    )
                                            )
                                    )
                            )
                    ),
                    OptionalInlineWhitespace(),
                    ')',
                    OptionalWhitespace(),
                    '{',
                    OptionalInlineWhitespace(),
                    '\n',
                    Action<Any> {
                        this[peek(2).toString()] = 0

                        return@Action true
                    },
                    Sequence(
                            saveState(),
                            OpenSpiralHeaderLines(),
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
                    OptionalWhitespace(),
                    "}"
            )
}