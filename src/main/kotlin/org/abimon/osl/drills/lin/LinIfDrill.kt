package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.lin.*
import org.parboiled.Action
import org.parboiled.BaseParser.NOTHING
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinIfDrill : DrillHead<Array<LinScript>> {
    object JOIN_BACK : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = --parser.flagCheckIndentation
            val branch = parser.data.remove("FLAG_CHECK_BRANCH_FOR_$indentation").toString().toIntOrNull() ?: 0
            return arrayOf(SetLabelEntry(128 * 256 + branch + 1))
        }
    }

    val cmd = "LIN-IF"
    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "if",
                    ZeroOrMore(Whitespace()),
                    "(",
                    ZeroOrMore(Whitespace()),
                    pushTmpAction(cmd, this@LinIfDrill),

                    OneOrMore(Digit()),
                    pushTmpAction(cmd),
                    ZeroOrMore(Whitespace()),
                    ",",

                    ZeroOrMore(Whitespace()),
                    OneOrMore(Digit()),
                    pushTmpAction(cmd),
                    ZeroOrMore(Whitespace()),

                    "==", //TODO: Eventually support other comparisons
                    pushTmpAction(cmd),

                    ZeroOrMore(Whitespace()),
                    OneOrMore(Digit()),
                    pushTmpAction(cmd),
                    ZeroOrMore(Whitespace()),

                    ')',
                    ZeroOrMore(Whitespace()),
                    "{",
                    '\n',
                    pushTmpStack(cmd),
                    OpenSpiralLines(),
                    Action<Any> { push(listOf(JOIN_BACK)) },
                    "}"
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
        val indent = parser.flagCheckIndentation++
        val branch = parser.branches
        parser.branches += 2

        parser["FLAG_CHECK_BRANCH_FOR_$indent"] = branch

        val flagPartA = rawParams[0].toString().toIntOrNull() ?: 0
        val flagPartB = rawParams[1].toString().toIntOrNull() ?: 0

        val operation = rawParams[2].toString()

        val comparison = rawParams[3].toString().toIntOrNull() ?: 0

        return arrayOf(
                CheckFlagAEntry(0x35, intArrayOf(flagPartA, flagPartB, 0, comparison)),
                EndFlagCheckEntry(),
                GoToLabelEntry(128 * 256 + branch),
                GoToLabelEntry(128 * 256 + branch + 1),

                SetLabelEntry(128 * 256 + branch)
        )
    }
}