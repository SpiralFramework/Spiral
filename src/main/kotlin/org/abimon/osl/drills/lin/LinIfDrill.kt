package org.abimon.osl.drills.lin

import org.abimon.osl.EnumLinFlagCheck
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
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
            val branch = parser.data.remove("FLAG_CHECK_BRANCH_FOR_$indentation").toString().toIntOrNull() ?: 0
            return when(parser.game) {
                DR1 -> arrayOf(SetLabelEntry(128 * 256 + branch + 2))
                DR2 -> arrayOf(SetLabelEntry(128 * 256 + branch + 2))
                else -> TODO("Flag Checks are not documented for ${parser.game}")
            }
        }
    }

    object JUMP_BACK : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = parser.flagCheckIndentation - 1
            val branch = parser["FLAG_CHECK_BRANCH_FOR_$indentation"].toString().toIntOrNull() ?: 0
            return when(parser.game) {
                DR1 -> arrayOf(GoToLabelEntry(128 * 256 + branch + 2))
                DR2 -> arrayOf(GoToLabelEntry(128 * 256 + branch + 2))
                else -> TODO("Flag Checks are not documented for ${parser.game}")
            }
        }
    }

    object ELSE : DrillHead<Array<LinScript>> {
        override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
        override fun OpenSpiralLanguageParser.syntax(): Rule = NOTHING

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
            val indentation = parser.flagCheckIndentation - 1
            val branch = parser["FLAG_CHECK_BRANCH_FOR_$indentation"].toString().toIntOrNull() ?: 0
            return when(parser.game) {
                DR1 -> arrayOf(SetLabelEntry(128 * 256 + branch + 1))
                DR2 -> arrayOf(SetLabelEntry(128 * 256 + branch + 1))
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
                    OptionalWhitespace(),
                    "(",
                    OptionalWhitespace(),
                    pushEmptyDrillHead(cmd, this@LinIfDrill),

                    Flag(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd),
                    OptionalWhitespace(),

                    FirstOf(EnumLinFlagCheck.NAMES),
                    pushTmpAction(cmd),

                    OptionalWhitespace(),
                    FlagValue(),
                    pushTmpFromStack(cmd),
                    OptionalWhitespace(),

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
        val branch = parser.labels
        parser.labels += 2

        parser["FLAG_CHECK_BRANCH_FOR_$indent"] = branch

        val flagPartA = rawParams[0].toString().toIntOrNull() ?: 0
        val flagPartB = rawParams[1].toString().toIntOrNull() ?: 0

        val operationName = rawParams[2].toString()
        val operation = EnumLinFlagCheck.values().first { enum -> operationName in enum.names }

        val comparison = rawParams[3].toString().toIntOrNull() ?: 0

        return when(parser.game) {
            DR1 -> arrayOf(
                    CheckFlagAEntry(0x35, intArrayOf(flagPartA, flagPartB, operation.flag, comparison)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(128 * 256 + branch),
                    GoToLabelEntry(128 * 256 + branch + 1),

                    SetLabelEntry(128 * 256 + branch)
            )
            DR2 -> arrayOf(
                    CheckFlagAEntry(0x35, intArrayOf(flagPartA, flagPartB, operation.flag, comparison)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(128 * 256 + branch),
                    GoToLabelEntry(128 * 256 + branch + 1),

                    SetLabelEntry(128 * 256 + branch)
            )
            else -> TODO("Flag Checks are not documented for ${parser.game}")
        }
    }
}