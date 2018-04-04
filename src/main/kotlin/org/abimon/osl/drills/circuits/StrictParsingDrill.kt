package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object StrictParsingDrill : DrillCircuit {
    val cmd = "STRICT-PARSING"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("Enable", "Disable"),
                    pushTmpAction(cmd, this@StrictParsingDrill),
                    pushTmpAction(cmd),
                    Whitespace(),
                    "Strict Parsing",
                    operateOnTmpActions(cmd) { stack -> operate(this, stack.drop(1).toTypedArray()) },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        parser.strictParsing = rawParams[0].toString().equals("enable", true)
    }
}