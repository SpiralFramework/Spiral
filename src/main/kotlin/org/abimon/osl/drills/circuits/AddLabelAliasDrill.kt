package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object AddLabelAliasDrill : DrillCircuit {
    val cmd = "ADD-LABEL-ALIAS"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Add label alias",
                    Whitespace(),
                    pushTmpAction(cmd, this@AddLabelAliasDrill),
                    Parameter(cmd),
                    Whitespace(),
                    "to",
                    Whitespace(),
                    Label(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd),
                    operateOnTmpActions(cmd) { params -> operate(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        val first = rawParams[1].toString().toIntOrNull() ?: 0
        val second = rawParams[2].toString().toIntOrNull() ?: 0

        val id = (first shl 8) or second

        parser.customLabelNames[rawParams[0].toString()] = id
    }
}