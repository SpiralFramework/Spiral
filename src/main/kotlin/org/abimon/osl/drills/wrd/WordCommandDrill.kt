package org.abimon.osl.drills.wrd

import org.abimon.osl.LineMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptCommand
import org.abimon.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordCommandDrill: DrillHead<WordScriptCommand> {
    val cmd = "WRD-COMMAND"
    val VALID = intArrayOf(1, 2, 3)

    override val klass: KClass<WordScriptCommand> = WordScriptCommand::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Word Command",
                    Whitespace(),
                    Digit(),
                    Action<Any> { (match().toIntOrNull() ?: 1) in VALID },
                    pushTmpAction(cmd, this@WordCommandDrill),
                    pushTmpAction(cmd),
                    ':',
                    ZeroOrMore(Whitespace()),
                    OneOrMore(LineMatcher),
                    pushTmpAction(cmd),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptCommand? {
        val num = rawParams[0].toString().toIntOrNull() ?: 1
        if (num !in VALID)
            return null

        return WordScriptCommand(num, rawParams[1].toString())
    }
}