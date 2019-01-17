package info.spiralframework.osl.drills.lin.headerCircuits

import info.spiralframework.osl.EnumLinFlagCheck
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.StringVar
import org.parboiled.support.Var

object LinWhenGameStateDrill : DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val flagToCheckVar = StringVar("")
        val operationVar = StringVar("")
        val valueToCheckVar = StringVar("")

        val returnBranchVar = Var(0)

        val checksVar = StringVar("")
        val headerLinesVar = StringVar("")

        val pushLinesAction = Action<Any> { context ->
            loadState(context)
            val label = findLabel()

            val lines = context.match

            checksVar.append("Check Game State:${flagToCheckVar.get()}, ${operationVar.get()}, ${valueToCheckVar.get()}\ngoto $label\n")
            return@Action headerLinesVar.append("Mark Label $label\n$lines\ngoto ${returnBranchVar.get()}\n")
        }

        return Sequence(
                FirstOf("g-when", "gameContext-when", "wheng", "when-g", "when-state", "when-s", "whens", "swhen", "state-when"),
                OptionalInlineWhitespace(),
                "(",
                OptionalInlineWhitespace(),
                ParameterButToStack(')'),
                Action<Any> { flagToCheckVar.set(pop().toString()) },
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
                                        FirstOf(info.spiralframework.osl.EnumLinFlagCheck.NAMES),
                                        Action<Any> { operationVar.set(match()) },
                                        OptionalInlineWhitespace()
                                ),
                                Action<Any> { operationVar.set("==") }
                        ),
                        ParameterToStack(),
                        Action<Any> { valueToCheckVar.set(pop().toString()) },
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
