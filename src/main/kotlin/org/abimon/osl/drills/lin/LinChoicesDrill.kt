package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.drills.DrillHead
import org.abimon.osl.drills.StaticDrill
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.*
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinChoicesDrill : DrillHead<Array<LinScript>> {
    val cmd = "LIN-CHOICES"
    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    Sequence(
                            "Display Choices",
                            Action<Any> {
                                if (cmd in tmpStack)
                                    System.err.println("Nested choice selection detected; this is unsupported! If you wish to nest choices, you will need to use labels.")
                                return@Action true
                            },
                            clearTmpStack(cmd),
                            pushDrillHead(cmd, this@LinChoicesDrill),
                            OptionalWhitespace(),
                            FirstOf(
                                    Sequence(
                                            '(',
                                            LinText(cmd, ')'),
                                            ')'
                                    ),
                                    pushTmpAction(cmd, "")
                            ),
                            OptionalWhitespace(),
                            '{',
                            '\n',
                            Action<Any> {
                                data["$cmd-LABEL"] = findLabel()
                                push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChangeUIEntry(18, 4)), "")))
                            },
                            OneOrMore(
                                    Sequence(
                                            OptionalWhitespace(),
                                            ParameterToStack(),
                                            Optional(
                                                    OptionalWhitespace(),
                                                    "->",
                                                    OptionalWhitespace()
                                            ),
                                            Action<Any> {
                                                val name = pop()
                                                val currentChoice = data["$cmd-CHOICE"]?.toString()?.toIntOrNull() ?: 0
                                                data["$cmd-CHOICE"] = currentChoice + 1

                                                push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChoiceEntry(currentChoice + 1)), "")))
                                                push(listOf(SpiralDrillBit(BasicLinTextDrill, ""), name))
                                                push(listOf(SpiralDrillBit(StaticDrill<LinScript>(when (game) {
                                                    DR1 -> WaitFrameEntry.DR1
                                                    DR2 -> WaitFrameEntry.DR2
                                                    else -> TODO("Unknown game $game (Text hasn't been completely documented!)")
                                                }), "")))
                                            },
                                            OptionalWhitespace(),
                                            '{',
                                            '\n',
                                            OpenSpiralLines(),
                                            Action<Any> {
                                                push(listOf(SpiralDrillBit(StaticDrill<LinScript>(GoToLabelEntry((data["$cmd-LABEL"] as Int))), "")))
                                            },
                                            '}',
                                            '\n',
                                            OptionalWhitespace()
                                    )
                            ),
                            '}',

                            Action<Any> {
                                pushTmp(cmd, data["$cmd-LABEL"] as Int)

                                this["$cmd-CHOICE"] = null
                                this["$cmd-LABEL"] = null

                                return@Action true
                            }
                    ),

                    pushStackWithHead(cmd),
                    Action<Any> {
                        if (cmd in tmpStack)
                            System.err.println("Nested choice selection detected; this is unsupported! If you wish to nest choices, you will need to use labels.")
                        return@Action true
                    }
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript>? {
        val label = rawParams[1].toString().toIntOrNull() ?: parser.findLabel()
        if (rawParams[0].toString().isBlank())
            return arrayOf(ChoiceEntry(18), ChoiceEntry(19), ChoiceEntry(255), SetLabelEntry(label))
        return when (parser.game) {
            DR1 -> arrayOf(
                    ChoiceEntry(18),
                    ChoiceEntry(19),
                    TextEntry(rawParams[0].toString(), -1),
                    WaitFrameEntry.DR1,
                    ChoiceEntry(255),
                    SetLabelEntry(label)
            )
            DR2 -> arrayOf(
                    ChoiceEntry(18),
                    ChoiceEntry(19),
                    TextEntry(rawParams[0].toString(), -1),
                    WaitFrameEntry.DR1,
                    ChoiceEntry(255),
                    SetLabelEntry(label)
            )
            else -> TODO("Choices are not documented for ${parser.game}")
        }
    }
}