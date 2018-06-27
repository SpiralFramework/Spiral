package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.SpiralDrillException
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.*
import org.parboiled.BaseParser
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinIfRandDrill : DrillHead<Array<LinScript>> {
    object JOIN_BACK : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = BaseParser.NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = --parser.flagCheckIndentation
            val branch = parser.data.remove("FLAG_JUMP_BRANCH_FOR_$indentation").toString().toIntOrNull()
                    ?: throw SpiralDrillException("No flag jump branch found for $indentation")
            return when (parser.gameContext) {
                DR1 -> arrayOf(SetLabelEntry(branch))
                DR2 -> arrayOf(SetLabelEntry(branch))
                else -> TODO("Flag Checks are not documented for ${parser.gameContext}")
            }
        }
    }

    object JUMP_BACK : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = BaseParser.NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = parser.flagCheckIndentation - 1
            val branch = parser["FLAG_JUMP_BRANCH_FOR_$indentation"].toString().toIntOrNull()
                    ?: throw SpiralDrillException("No flag jump branch found for $indentation")
            return when (parser.gameContext) {
                DR1 -> arrayOf(GoToLabelEntry(branch))
                DR2 -> arrayOf(GoToLabelEntry(branch))
                else -> TODO("Flag Checks are not documented for ${parser.gameContext}")
            }
        }
    }

    object ELSE : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = BaseParser.NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = parser.flagCheckIndentation - 1
            val branch = parser["FLAG_ELSE_BRANCH_FOR_$indentation"].toString().toIntOrNull()
                    ?: throw SpiralDrillException("No flag else branch found for $indentation")
            return when (parser.gameContext) {
                DR1 -> arrayOf(SetLabelEntry(branch))
                DR2 -> arrayOf(SetLabelEntry(branch))
                else -> TODO("Flag Checks are not documented for ${parser.gameContext}")
            }
        }
    }

    val cmd = "LIN-IF-GAME"
    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("rif", "rand-if", "ifr", "if-random", "if-rand", "if-r"),
                    pushEmptyDrillHead(cmd, this@LinIfRandDrill),
                    FirstOf(
                            Sequence(
                                    OptionalInlineWhitespace(),
                                    '(',
                                    OptionalInlineWhitespace(),
                                    RuleWithVariables(OneOrMore(Digit())),
                                    OptionalInlineWhitespace(),
                                    ')'
                            ),
                            pushToStack(50)
                    ),
                    pushTmpFromStack(cmd),
                    OptionalWhitespace(),
                    "{",
                    '\n',
                    pushTmpStack(cmd),
                    OpenSpiralLines(),
                    pushAction(listOf(SpiralDrillBit(JUMP_BACK, ""))),
                    "}",
                    pushAction(listOf(SpiralDrillBit(ELSE, ""))),
                    Optional(
                            Whitespace(),
                            "else",
                            Whitespace(),
                            "{",
                            '\n',
                            OpenSpiralLines(),
                            "}"
                    ),
                    pushAction(listOf(SpiralDrillBit(JOIN_BACK, "")))
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
        val indent = parser.flagCheckIndentation++
        val jumpTo = parser.findLabel()
        val jumpElse = parser.findLabel()
        val ifTrue = parser.findLabel()

        parser["FLAG_JUMP_BRANCH_FOR_$indent"] = jumpTo
        parser["FLAG_ELSE_BRANCH_FOR_$indent"] = jumpElse

        val chance = rawParams[0].toString().toIntOrNull() ?: 50

        return when (parser.gameContext) {
            DR1 -> arrayOf(
                    UnknownEntry(0x36, intArrayOf(0, 14, 2, 0, chance)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(ifTrue),
                    GoToLabelEntry(jumpElse),

                    SetLabelEntry(ifTrue)
            )
            DR2 -> arrayOf(
                    UnknownEntry(0x36, intArrayOf(0, 14, 2, 0, chance)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(ifTrue),
                    GoToLabelEntry(jumpElse),

                    SetLabelEntry(ifTrue)
            )
            else -> TODO("Flag Checks are not documented for ${parser.gameContext}")
        }
    }
}