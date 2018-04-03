package org.abimon.osl.drills.wrd

import org.abimon.osl.LineMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptString
import org.abimon.osl.drills.DrillHead
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordStringDrill: DrillHead<WordScriptString> {
    val cmd = "WRD-STRING"
    val VALID = intArrayOf(1, 2, 3)

    override val klass: KClass<WordScriptString> = WordScriptString::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Word String:",
                    pushTmpAction(cmd, this@WordStringDrill),
                    ZeroOrMore(Whitespace()),
                    OneOrMore(LineMatcher),
                    pushTmpAction(cmd),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptString = WordScriptString(rawParams[0].toString())
}