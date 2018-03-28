package org.abimon.osl.drills.circuits

import org.abimon.osl.LineCodeMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Action
import org.parboiled.Rule

object EchoDrill: DrillCircuit {
    val cmd = "ECHO"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "echo",
                    Action<Any> { true },
                    Whitespace(),
                    pushTmpAction(cmd, this@EchoDrill),
                    OneOrMore(LineCodeMatcher),
                    pushTmpAction(cmd),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        println(rawParams[0])
    }
}