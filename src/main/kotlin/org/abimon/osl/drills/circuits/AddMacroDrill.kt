package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object AddMacroDrill : DrillCircuit {
    val cmd = "ADD_MACRO"
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf(
                                    "Add Macro",
                                    "Define Macro"
                            ),
                            Whitespace(),
                            pushDrillHead(cmd, this@AddMacroDrill),
                            Parameter(cmd),
                            Whitespace(),
                            "as",
                            Whitespace(),
                            Parameter(cmd),
                            ZeroOrMore(
                                    Whitespace(),
                                    FirstOf("or", "and"),
                                    Whitespace(),
                                    Parameter(cmd)
                            ),
                            operateOnTmpActions(cmd) { params -> operate(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) }
                    ),
                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        val macro = rawParams[0].toString()

        val result = parser.copy().parse("OSL Script\n$macro")
        if (result.hasErrors())
            return

        val macroData = result.valueStack.reversed()

        for (i in 1 until rawParams.size)
            parser.macros[rawParams[i].toString()] = macroData
    }
}