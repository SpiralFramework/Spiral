package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object AddAnimationAliasDrill : DrillCircuit {
    val cmd = "ADD-ANIMATION-ALIAS"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            "Add animation alias",
                            InlineWhitespace(),
                            pushDrillHead(cmd, this@AddAnimationAliasDrill),
                            Parameter(cmd),
                            InlineWhitespace(),
                            "to",
                            InlineWhitespace(),
                            AnimationID(),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd),
                            operateOnTmpActions(cmd) { params -> operate(this, params.toTypedArray().let { array -> array.copyOfRange(1, array.size) }) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        val major = rawParams[1].toString().toIntOrNull() ?: 0
        val minor = rawParams[2].toString().toIntOrNull() ?: 0

        val id = (major shl 8) or minor

        parser.customAnimationNames[rawParams[0].toString()] = id
    }
}