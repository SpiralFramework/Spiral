package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.ValueStack

object HeaderOSLDrill: DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    FirstOf(
                            Sequence(
                                    "Header:",
                                    OptionalWhitespace()
                            ),
                            Sequence(
                                    "Load Header",
                                    Whitespace()
                            )
                    ),
                    ParameterToStack(),
                    Action<Any> {
                        val stack = loadStack(this, pop().toString())?.toList() ?: return@Action false
                        push(stack)
                    },

                    Action<Any> { context ->
                        val stack = context.valueStack.pop() as? List<*> ?: return@Action false
                        for (value in stack.reversed()) {
                            if (value is List<*>) {
                                val head = (value[0] as? DrillHead<*>) ?: continue
                                val headParams = value.subList(1, value.size).filterNotNull().toTypedArray()
                                head.operate(this@syntax, headParams)
                                push(value)
                            }
                        }
                        return@Action true
                    }
            )

    fun loadStack(parser: OpenSpiralLanguageParser, headerFile: String): ValueStack<*>? {

        if (parser.flags["Header-$headerFile-Loaded"] != true) {
            val data = parser.load(headerFile) ?: return null
            val result = parser.copy().parse(String(data))
            if (!result.hasErrors())
                return result.valueStack
        }

        return null
    }
}