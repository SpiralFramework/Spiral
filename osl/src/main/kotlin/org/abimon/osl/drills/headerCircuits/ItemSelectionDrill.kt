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
object ItemSelectionDrill : DrillCircuit {
    val DIGITS = "\\d+".toRegex()

    val MISSING_ITEMS = arrayOf(
            "missing", "absent", "absent item", "absent items",
            "missing item", "missing items", "on item missing",
            "on items missing", "on item absent", "on items absent",
            "key item", "key items"
    )

    val DEFAULT_MISSING = buildString {
        appendln("Change UI|2, 0")
        appendln("Change UI|1, 1")
        appendln("Narrator: &{green} Sorry, but we can't let you give him that. It's what\\nwe in the industry like to call a &{yellow} key item &{blue} .")
    }

    val OPENING_LINES = arrayOf(
            "opening", "selection", "on open", "opening lines"
    )

    val DEFAULT_OPENING = buildString {
        appendln("Change UI|2, 0")
        appendln("Change UI|1, 1")
        appendln("Speaker|Narrator")
        appendln("Text| &{green} What would you like to give them?")
    }

    val ON_EXIT = arrayOf(
            "on_exit", "exiting", "exit", "on exit", "close", "closing", "on close", "cancel", "cancelling", "on cancel"
    )

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val startLabel = Var<Int>()
        val endLabel = Var<Int>()
        val itemIDs = Var<MutableSet<String>>(HashSet())
        val choices = StringVar()
        val missingItems = StringVar()
        val onExit = StringVar()
        val openingLines = StringVar()

        return Sequence(
                "Select ",
                FirstOf("Present", "Presents", "Item", "Items"),
                OptionalInlineWhitespace(),
                '{',
                '\n',
                Action<Any> { choices.set("") },
                Action<Any> { startLabel.set(findLabel()) },
                Action<Any> { endLabel.set(findLabel()) },
                Action<Any> { missingItems.set("\"missing\" -> {\n$DEFAULT_MISSING\ngoto ${startLabel.get()}\n}") },
                Action<Any> { onExit.set("\"cancel\" -> {\ngoto ${endLabel.get()}\n}") },
                Action<Any> { openingLines.set("\"opening\" -> {\n$DEFAULT_OPENING\n}") },
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
                                            return@Action missingItems.set("\"missing\" -> {\n$lines\n}\n")
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
                                            return@Action name in ON_EXIT
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
                                            return@Action onExit.set("\"cancel\" -> {\n$lines\n}\n")
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
                                            return@Action openingLines.set("\"opening\" -> {\n$lines\n}\n")
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
                        appendln("[Internal] Select Item {")
                        appendln(choices.get())
                        appendln(missingItems.get())
                        appendln(openingLines.get())
                        appendln(onExit.get())
                        appendln("}")
                        appendln("Mark Label ${endLabel.get()}")
                    }))
                    return@Action true
                }
        )
    }
}