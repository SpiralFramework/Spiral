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

object LinIfGameStateDrill : DrillHead<Array<LinScript>> {
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

    val cmd = "LIN-IF-GAME"
    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("g-if", "game-if", "ifg", "if-g", "if-s", "ifs", "sif", "state-if", "if-state"),
                    OptionalWhitespace(),
                    "(",
                    OptionalWhitespace(),
                    pushEmptyDrillHead(cmd, this@LinIfGameStateDrill),

                    GameState(),
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
        val jumpTo = parser.findLabel()
        val jumpElse = parser.findLabel()
        val ifTrue = parser.findLabel()

        parser["FLAG_JUMP_BRANCH_FOR_$indent"] = jumpTo
        parser["FLAG_ELSE_BRANCH_FOR_$indent"] = jumpElse

        val state = rawParams[0].toString().toIntOrNull() ?: 0

        val operationName = rawParams[1].toString()
        val operation = EnumLinFlagCheck.values().first { enum -> operationName in enum.names }

        val comparison = rawParams[2].toString().toIntOrNull() ?: 0

        return when(parser.game) {
            DR1 -> arrayOf(
                    UnknownEntry(0x36, intArrayOf(0, state, operation.flag, 0, comparison)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(ifTrue),
                    GoToLabelEntry(jumpElse),

                    SetLabelEntry(ifTrue)
            )
            DR2 -> arrayOf(
                    UnknownEntry(0x36, intArrayOf(0, state, operation.flag, 0, comparison)),
                    EndFlagCheckEntry(),
                    GoToLabelEntry(ifTrue),
                    GoToLabelEntry(jumpElse),

                    SetLabelEntry(ifTrue)
            )
            else -> TODO("Flag Checks are not documented for ${parser.game}")
        }
    }
}