package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.runWith
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.StringVar
import org.parboiled.support.Var
import java.util.*

/** Probably one of the more complicated codes */
object EvidenceSelectionDrill : DrillCircuit {
    val DIGITS = "\\d+".toRegex()

    val MISSING_ITEMS = arrayOf(
            "missing", "absent", "absent evidence",
            "missing evidence", "on evidence missing",
            "on evidence absent", "wrong", "incorrect",
            "on wrong", "on incorrect", "on wrong evidence",
            "on incorrect evidence",
            "wrong evidence", "incorrect evidence"
    )

    val OPENING_LINES = arrayOf(
            "opening", "selection", "on open", "opening lines"
    )

    val DEFAULT_OPENING = buildString {
        appendln("Text|&{blue} The evidence I'm looking for is...")
        appendln("Wait Frame|")
    }

    val TIMEOUT = arrayOf(
            "timeout", "on wait", "waiting",
            "afk", "on timeout"
    )

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val startLabel = Var<Int>()
        val endLabel = Var<Int>()
        val itemIDs = Var<MutableSet<String>>(HashSet())
        val choices = StringVar()
        val missingEvidence = StringVar()
        val onTimeout = StringVar()
        val openingLines = StringVar()

        return Sequence(
                "Select ",
                FirstOf("Evidence", "Truth Bullet", "Truth Bullets"),
                OptionalInlineWhitespace(),
                '{',
                '\n',
                Action<Any> { choices.set("") },
                Action<Any> { startLabel.set(findLabel()) },
                Action<Any> { endLabel.set(findLabel()) },
                Action<Any> { missingEvidence.set("\"missing\" -> {\nChange UI|4, 0\ngoto ${startLabel.get()}\n}") },
                Action<Any> { onTimeout.set("\"timeout\" -> {\nChange UI|4, 0\ngoto ${endLabel.get()}\n}") },
                Action<Any> { openingLines.set("\"opening\" -> {\nChange UI|4, 1\n$DEFAULT_OPENING\n}") },
                OneOrMore(
                        FirstOf(
                                Sequence(
                                        OptionalWhitespace(),
                                        ParameterToStack(),
                                        Action<Any> {
                                            val name = pop().toString().toLowerCase()
                                            return@Action name in MISSING_ITEMS
                                        },
                                        Optional(
                                                OptionalInlineWhitespace(),
                                                "->"
                                        ),
                                        OptionalInlineWhitespace(),
                                        '{',
                                        '\n',
                                        saveState(),
                                        OpenSpiralHeaderLines(),
                                        Action<Any> { context ->
                                            loadState(context)

                                            val lines = context.match
                                                    .replace("\$START_LABEL", startLabel.get().toString())
                                                    .replace("\$END_LABEL", endLabel.get().toString())
                                                    .replace("%START_LABEL", startLabel.get().toString())
                                                    .replace("%END_LABEL", endLabel.get().toString())
                                            return@Action missingEvidence.set("\"missing\" -> {\nChange UI|4, 0\n$lines\n}\n")
                                        },
                                        OptionalWhitespace(),
                                        '}',
                                        '\n',
                                        OptionalWhitespace()
                                ),
                                Sequence(
                                        OptionalWhitespace(),
                                        ParameterToStack(),
                                        Action<Any> {
                                            val name = pop().toString().toLowerCase()
                                            return@Action name in TIMEOUT
                                        },
                                        Optional(
                                                OptionalInlineWhitespace(),
                                                "->"
                                        ),
                                        OptionalInlineWhitespace(),
                                        '{',
                                        '\n',
                                        saveState(),
                                        OpenSpiralHeaderLines(),
                                        Action<Any> { context ->
                                            loadState(context)

                                            val lines = context.match
                                                    .replace("\$START_LABEL", startLabel.get().toString())
                                                    .replace("\$END_LABEL", endLabel.get().toString())
                                                    .replace("%START_LABEL", startLabel.get().toString())
                                                    .replace("%END_LABEL", endLabel.get().toString())
                                            return@Action onTimeout.set("\"timeout\" -> {\nChange UI|4, 0\n$lines\n}\n")
                                        },
                                        OptionalWhitespace(),
                                        '}',
                                        '\n',
                                        OptionalWhitespace()
                                ),
                                Sequence(
                                        OptionalWhitespace(),
                                        ParameterToStack(),
                                        Action<Any> {
                                            val name = pop().toString().toLowerCase()
                                            return@Action name in OPENING_LINES
                                        },
                                        Optional(
                                                OptionalInlineWhitespace(),
                                                "->"
                                        ),
                                        OptionalInlineWhitespace(),
                                        '{',
                                        '\n',
                                        saveState(),
                                        OpenSpiralHeaderLines(),
                                        Action<Any> { context ->
                                            loadState(context)

                                            val lines = context.match
                                                    .replace("\$START_LABEL", startLabel.get().toString())
                                                    .replace("\$END_LABEL", endLabel.get().toString())
                                                    .replace("%START_LABEL", startLabel.get().toString())
                                                    .replace("%END_LABEL", endLabel.get().toString())
                                            return@Action openingLines.set("\"opening\" -> {\nChange UI|4, 1\n$lines\n}\n")
                                        },
                                        OptionalWhitespace(),
                                        '}',
                                        '\n',
                                        OptionalWhitespace()
                                ),
                                Sequence(
                                        OptionalWhitespace(),
                                        itemIDs.runWith { list -> list.clear() },
                                        Sequence(
                                                ParameterToStack(),
                                                Action<Any> { itemIDs.get().add(pop().toString()) },
                                                ZeroOrMore(
                                                        ",",
                                                        OptionalInlineWhitespace(),
                                                        ParameterToStack(),
                                                        Action<Any> { itemIDs.get().add(pop().toString()) }
                                                )
                                        ),
                                        Optional(
                                                OptionalInlineWhitespace(),
                                                "->"
                                        ),
                                        OptionalInlineWhitespace(),
                                        '{',
                                        '\n',
                                        saveState(),
                                        OpenSpiralHeaderLines(),
                                        Action<Any> { context ->
                                            loadState(context)

                                            val lines = context.match
                                                    .replace("\$START_LABEL", startLabel.get().toString())
                                                    .replace("\$END_LABEL", endLabel.get().toString())
                                                    .replace("%START_LABEL", startLabel.get().toString())
                                                    .replace("%END_LABEL", endLabel.get().toString())
                                            val names = itemIDs.get()

                                            names.forEach { name -> choices.append(buildString {
                                                if (name.matches(DIGITS))
                                                    append(name)
                                                else
                                                    append("\"$name\"")

                                                appendln(" -> {")
                                                appendln("Change UI|4, 0")
                                                appendln(lines)
                                                appendln("goto ${endLabel.get()}")
                                                appendln("}")
                                            }) }

                                            return@Action true
                                        },
                                        OptionalWhitespace(),
                                        '}',
                                        '\n',
                                        OptionalWhitespace()
                                )
                        )
                ),
                '}',
                Action<Any> {
                    push(arrayOf(this, buildString {
                        appendln("Mark Label ${startLabel.get()}")
                        appendln("[Internal] Select Evidence {")
                        appendln(choices.get())
                        appendln(missingEvidence.get())
                        appendln(openingLines.get())
                        appendln(onTimeout.get())
                        appendln("}")
                        appendln("Mark Label ${endLabel.get()}")
                    }))
                    return@Action true
                }
        )
    }
}