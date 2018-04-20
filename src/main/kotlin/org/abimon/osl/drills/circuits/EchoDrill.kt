package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object EchoDrill : DrillCircuit {
    val cmd = "ECHO"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            "echo",
                            Whitespace(),
                            pushDrillHead(cmd, this@EchoDrill),
                            Parameter(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        println(rawParams[0])
    }
}