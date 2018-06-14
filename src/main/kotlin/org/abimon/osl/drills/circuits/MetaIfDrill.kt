package org.abimon.osl.drills.circuits

import org.abimon.osl.EnumMetaIfOperations
import org.abimon.osl.EnumMetaJoiners
import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object MetaIfDrill : DrillCircuit {
    val cmd = "META-IF"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("mif", "meta-if", "ifm"),
                    OptionalInlineWhitespace(),
                    "(",
                    ZeroOrMore(Sequence(Comparison(), FirstOf(EnumMetaJoiners.NAMES), operateOnTmpStack("$cmd-COMPARISON") { value -> pushTmp(cmd, value) }, pushTmpAction(cmd))),
                    Comparison(),
                    operateOnTmpStack("$cmd-COMPARISON") { value -> pushTmp(cmd, value) },
                    ')',
                    OptionalInlineWhitespace(),
                    "{",
                    '\n',
                    Sequence(
                            clearStateAction(),
                            operateOnTmpActionsWithContext(cmd) { context, stack ->
                                if (!evaluate(this, stack))
                                    saveState(context)
                            },
                            OpenSpiralLines(),
                            loadState(),
                            clearTmpStack(cmd)
                    ),
                    "}"
            )

    fun OpenSpiralLanguageParser.Comparison(): Rule =
            Sequence(
                    clearTmpStack("$cmd-COMPARISON"),
                    OptionalInlineWhitespace(),

                    Parameter("$cmd-COMPARISON"),
                    InlineWhitespace(),

                    FirstOf(EnumMetaIfOperations.NAMES),
                    pushTmpAction("$cmd-COMPARISON"),

                    InlineWhitespace(),
                    ParameterBut("$cmd-COMPARISON", ')'),
                    OptionalInlineWhitespace()
            )

    fun evaluate(parser: OpenSpiralLanguageParser, params: List<Any>): Boolean {
        val firstVariable = params[0].toString()
        val firstComparisonStr = params[1].toString()
        val firstComparison = EnumMetaIfOperations.values().first { enum -> firstComparisonStr in enum.names }
        val firstValue = params[2].toString()

        var result = firstComparison(parser, firstVariable, firstValue)

        for (i in 3 until params.size step 4) {
            val comparerStr = params[i].toString()
            val comparer = EnumMetaJoiners.values().first { enum -> comparerStr in enum.names }

            val variable = params[i + 1].toString()
            val comparisonStr = params[i + 2].toString()
            val comparison = EnumMetaIfOperations.values().first { enum -> comparisonStr in enum.names }
            val value = params[i + 3].toString()

            result = comparer(result, comparison(parser, variable, value))
        }

        return result
    }
}