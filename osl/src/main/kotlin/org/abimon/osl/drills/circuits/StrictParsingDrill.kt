package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.lin.BasicLinSpiralDrill
import org.parboiled.Rule

object StrictParsingDrill : DrillCircuit {
    val cmd = "STRICT-PARSING"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            Sequence(
                                    FirstOf("Enable", "Disable"),
                                    InlineWhitespace(),
                                    "Strict Parsing"
                            ),
                            pushDrillHead(cmd, this@StrictParsingDrill),
                            pushTmpAction(cmd),
                            operateOnTmpActions(cmd) { stack -> operate(this, stack.drop(1).toTypedArray()) }
                    ),

                    pushStackWithHead(BasicLinSpiralDrill.cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        parser.strictParsing = rawParams[0].toString().split("\\s+".toRegex())[0].equals("enable", true)
    }
}