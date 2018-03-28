package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.ValueStack

object HeaderOSLDrill: DrillCircuit {
    val cmd = "HEADER"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Header:",
                    Whitespace(),
                    pushTmpAction(cmd, this@HeaderOSLDrill),
                    Parameter(cmd),
                    operateOnTmpActions(cmd) { params ->
                        val stack = (loadStack(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) ?: return@operateOnTmpActions).toList()
                        push(stack)
                    },
                    clearTmpStack(cmd),

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

    fun loadStack(parser: OpenSpiralLanguageParser, params: Array<Any>): ValueStack<*>? {
        val headerFile = params[0].toString()

        if (parser.flags["Header-$headerFile-Loaded"] != true) {
            val data = parser.load(headerFile) ?: return null
            val result = parser.copy().parse(String(data))
            if (!result.hasErrors())
                return result.valueStack
        }

        return null
    }
}