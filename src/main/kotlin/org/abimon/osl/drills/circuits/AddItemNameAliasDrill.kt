package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object AddItemNameAliasDrill : DrillCircuit {
    val cmd = "ADD-ITEM-NAME-ALIAS"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            "Add item ",
                            Optional("name "),
                            "alias ",
                            pushDrillHead(cmd, this@AddItemNameAliasDrill),
                            Parameter(cmd),
                            InlineWhitespace(),
                            "to",
                            InlineWhitespace(),
                            ItemID(),
                            pushTmpFromStack(cmd),
                            operateOnTmpActions(cmd) { params -> operate(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        parser.customItemNames[rawParams[0].toString()] = rawParams[1].toString().toIntOrNull() ?: 0
    }
}