package org.abimon.osl.drills.circuits

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
                    Parameter(cmd),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        println(rawParams[0])
    }
}