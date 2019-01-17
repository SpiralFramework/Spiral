package org.abimon.osl.drills.lin.headerCircuits

import org.abimon.osl.EnumLinFlagCheck
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.StringVar
import org.parboiled.support.Var

object LinWhenDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val flagToCheckVar = Var(0)
        val operationVar = Var(0)
        val valueToCheckVar = Var(0)

        val returnBranchVar = Var(0)

        val checksVar = StringVar("")
        val headerLinesVar = StringVar("")

        val pushLinesAction = Action<Any> { context ->
            loadState(context)
            val label = findLabel()

            val lines = context.match

            checksVar.append("Check Flag A|${flagToCheckVar.get() shr 8}, ${flagToCheckVar.get() and 0xFF}, ${operationVar.get()}, ${valueToCheckVar.get()}\nEnd Flag Check|\ngoto $label\n")
            return@Action headerLinesVar.append("Mark Label $label\n$lines\ngoto ${returnBranchVar.get()}\n")
        }

        return Sequence(
                "when",
                OptionalInlineWhitespace(),
                "(",
                OptionalInlineWhitespace(),
                Flag(),
                Action<Any> {
                    flagToCheckVar.set((pop().toString().toIntOrNull() ?: 0) or ((pop().toString().toIntOrNull()
                            ?: 0) shl 8))
                },
                OptionalInlineWhitespace(),
                ")",
                OptionalWhitespace(),
                "{",
                '\n',
                Action<Any> {
                    returnBranchVar.set(findLabel())
                    checksVar.set("")
                    headerLinesVar.set("")
                },
                OneOrMore(
                        Action<Any> { true },
                        OptionalInlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        FirstOf(EnumLinFlagCheck.NAMES),
                                        Action<Any> { operationVar.set(EnumLinFlagCheck.values().first { enum -> match() in enum.names }.flag) },
                                        OptionalInlineWhitespace()
                                ),
                                Action<Any> { operationVar.set(EnumLinFlagCheck.EQUALS.flag) }
                        ),
                        FlagValue(),
                        Action<Any> { valueToCheckVar.set(pop().toString().toIntOrNull() ?: 0) },
                        Optional(
                                OptionalInlineWhitespace(),
                                "->"
                        ),
                        OptionalInlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        "{",
                                        '\n',
                                        OpenSpiralHeaderLines(),
                                        pushLinesAction,
                                        OptionalWhitespace(),
                                        "}"
                                ),
                                Sequence(
                                        SpiralHeaderLine(),
                                        pushLinesAction
                                )
                        ),
                        "\n",
                        OptionalWhitespace()
                ),
                "}",
                Action<Any> {
                    push(arrayOf(this, buildString {
                        appendln(checksVar.get())
                        appendln(headerLinesVar.get())
                    }))
                    return@Action true
                }
        )
    }
}