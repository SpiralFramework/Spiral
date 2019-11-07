package info.spiralframework.osl.drills.lin

import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.*
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.SpiralDrillException
import info.spiralframework.osl.drills.DrillHead
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
            return when (parser.hopesPeakGame) {
                DR1 -> arrayOf(SetLabelEntry.DR1(branch))
                DR2 -> arrayOf(SetLabelEntry.DR2(branch))
                else -> TODO("Flag Checks are not documented for ${parser.hopesPeakGame}")
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
            return when (parser.hopesPeakGame) {
                DR1 -> arrayOf(GoToLabelEntry.DR1(branch))
                DR2 -> arrayOf(GoToLabelEntry.DR1(branch))
                else -> TODO("Flag Checks are not documented for ${parser.hopesPeakGame}")
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
            return when (parser.hopesPeakGame) {
                DR1 -> arrayOf(SetLabelEntry.DR1(branch))
                DR2 -> arrayOf(SetLabelEntry.DR2(branch))
                else -> TODO("Flag Checks are not documented for ${parser.hopesPeakGame}")
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

        return when (parser.hopesPeakGame) {
            DR1 -> arrayOf(
                    UnknownEntry(0x36, intArrayOf(0, 14, 2, 0, chance)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry.DR1(ifTrue),
                    GoToLabelEntry.DR1(jumpElse),

                    SetLabelEntry.DR1(ifTrue)
            )
            DR2 -> arrayOf(
                    UnknownEntry(0x36, intArrayOf(0, 14, 2, 0, chance)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry.DR2(ifTrue),
                    GoToLabelEntry.DR2(jumpElse),

                    SetLabelEntry.DR2(ifTrue)
            )
            else -> TODO("Flag Checks are not documented for ${parser.hopesPeakGame}")
        }
    }
}
