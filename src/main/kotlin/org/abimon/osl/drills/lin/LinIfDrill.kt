package org.abimon.osl.drills.lin

import org.abimon.osl.EnumLinFlagCheck
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.SpiralDrillException
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.*
import org.parboiled.BaseParser.NOTHING
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinIfDrill : DrillHead<Array<LinScript>> {
    object JOIN_BACK : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = --parser.flagCheckIndentation
            val branch = parser.data.remove("FLAG_JUMP_BRANCH_FOR_$indentation").toString().toIntOrNull() ?: throw SpiralDrillException("No flag jump branch found for $indentation")
            return when(parser.game) {
                DR1 -> arrayOf(SetLabelEntry(branch))
                DR2 -> arrayOf(SetLabelEntry(branch))
                else -> TODO("Flag Checks are not documented for ${parser.game}")
            }
        }
    }

    object JUMP_BACK : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = parser.flagCheckIndentation - 1
            val branch = parser["FLAG_JUMP_BRANCH_FOR_$indentation"].toString().toIntOrNull() ?: throw SpiralDrillException("No flag jump branch found for $indentation")
            return when(parser.game) {
                DR1 -> arrayOf(GoToLabelEntry(branch))
                DR2 -> arrayOf(GoToLabelEntry(branch))
                else -> TODO("Flag Checks are not documented for ${parser.game}")
            }
        }
    }

    object ELSE : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = parser.flagCheckIndentation - 1
            val branch = parser["FLAG_ELSE_BRANCH_FOR_$indentation"].toString().toIntOrNull() ?: throw SpiralDrillException("No flag else branch found for $indentation")
            return when(parser.game) {
                DR1 -> arrayOf(SetLabelEntry(branch))
                DR2 -> arrayOf(SetLabelEntry(branch))
                else -> TODO("Flag Checks are not documented for ${parser.game}")
            }
        }
    }

    val cmd = "LIN-IF"
    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "if",
                    OptionalInlineWhitespace(),
                    "(",
                    OptionalInlineWhitespace(),
                    pushEmptyDrillHead(cmd, this@LinIfDrill),

                    Flag(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd),
                    OptionalInlineWhitespace(),

                    FirstOf(EnumLinFlagCheck.NAMES),
                    pushTmpAction(cmd),

                    OptionalInlineWhitespace(),
                    FlagValue(),
                    pushTmpFromStack(cmd),
                    OptionalInlineWhitespace(),

                    ')',
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

        val flagPartA = rawParams[0].toString().toIntOrNull() ?: 0
        val flagPartB = rawParams[1].toString().toIntOrNull() ?: 0

        val operationName = rawParams[2].toString()
        val operation = EnumLinFlagCheck.values().first { enum -> operationName in enum.names }

        val comparison = rawParams[3].toString().toIntOrNull() ?: 0

        return when(parser.game) {
            DR1 -> arrayOf(
                    CheckFlagAEntry(0x35, intArrayOf(flagPartA, flagPartB, operation.flag, comparison)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(ifTrue),
                    GoToLabelEntry(jumpElse),

                    SetLabelEntry(ifTrue)
            )
            DR2 -> arrayOf(
                    CheckFlagAEntry(0x35, intArrayOf(flagPartA, flagPartB, operation.flag, comparison)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(ifTrue),
                    GoToLabelEntry(jumpElse),

                    SetLabelEntry(ifTrue)
            )
            else -> TODO("Flag Checks are not documented for ${parser.game}")
        }
    }
}