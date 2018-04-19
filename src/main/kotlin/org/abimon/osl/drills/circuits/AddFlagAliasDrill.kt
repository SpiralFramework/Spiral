package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object AddFlagAliasDrill : DrillCircuit {
    val cmd = "ADD-FLAG-ALIAS"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Add flag alias",
                    Whitespace(),
                    pushTmpAction(cmd, this@AddFlagAliasDrill),
                    Parameter(cmd),
                    Whitespace(),
                    "to",
                    Whitespace(),
                    Flag(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd),
                    operateOnTmpActions(cmd) { params -> operate(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        val group = rawParams[1].toString().toIntOrNull() ?: 0
        val flagID = rawParams[2].toString().toIntOrNull() ?: 0

        val id = (group shl 8) or flagID

        parser.customFlagNames[rawParams[0].toString()] = id
    }
}