package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptString
import org.abimon.osl.drills.DrillHead
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordStringDrill : DrillHead<WordScriptString> {
    val cmd = "WRD-STRING"

    override val klass: KClass<WordScriptString> = WordScriptString::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Word String",
                            pushDrillHead(cmd, this@WordStringDrill),
                            FirstOf(
                                    Sequence(
                                            Whitespace(),
                                            "ID",
                                            AnyOf(":|"),
                                            OptionalWhitespace(),
                                            RuleWithVariables(OneOrMore(Digit())),
                                            pushTmpFromStack(cmd)
                                    ),
                                    pushTmpAction(cmd, "-1")
                            ),
                            AnyOf(":|"),
                            OptionalInlineWhitespace(),
                            WrdText(cmd),
                            operateOnTmpActions(cmd) { stack -> operate(this, stack.drop(1).toTypedArray()) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptString {
        val index = rawParams[0].toString().toIntOrNull() ?: -1
        val string = rawParams[1].toString()
        if (string !in parser.wordScriptStrings)
            parser.wordScriptStrings.add(string)
        return WordScriptString(string, index)
    }
}