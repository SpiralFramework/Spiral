package org.abimon.osl.drills.circuits

import org.abimon.osl.EnumMetaIfOperations
import org.abimon.osl.OpenSpiralLanguageParser
import org.parboiled.Action
import org.parboiled.Rule

object MetaIfDrill : DrillCircuit {
    val cmd = "META-IF"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("mif", "meta-if", "ifm"),
                    ZeroOrMore(Whitespace()),
                    "(",
                    ZeroOrMore(Whitespace()),

                    Parameter(cmd),
                    OneOrMore(Whitespace()),

                    FirstOf(EnumMetaIfOperations.NAMES),
                    pushTmpAction(cmd),

                    OneOrMore(Whitespace()),
                    ParameterBut(cmd, ')'),
                    ZeroOrMore(Whitespace()),
                    ')',
                    ZeroOrMore(Whitespace()),
                    "{",
                    '\n',
                    FirstOf(
                            Sequence(
                                    Action<Any> { silence },
                                    OpenSpiralLines()
                            ),
                            Sequence(
                                    operateOnTmpActions(cmd) { stack ->
                                        if (!evaluate(this, stack))
                                            silence = true
                                    },
                                    OpenSpiralLines(),
                                    Action<Any> {
                                        silence = false
                                        return@Action true
                                    },
                                    clearTmpStack(cmd)
                            )
                    ),
                    "}"
            )

    fun evaluate(parser: OpenSpiralLanguageParser, params: List<Any>): Boolean {
        val variable = params[0].toString()
        val comparisonStr = params[1].toString()
        val comparison = EnumMetaIfOperations.values().first { enum -> comparisonStr in enum.names }
        val value = params[2].toString()

        println("[$variable] $comparison [$value]")
        return comparison(parser, variable, value)
    }
}