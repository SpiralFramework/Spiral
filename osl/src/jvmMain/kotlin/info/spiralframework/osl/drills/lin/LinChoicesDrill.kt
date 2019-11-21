package info.spiralframework.osl.drills.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.*
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.osl.drills.StaticDrill
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinChoicesDrill : DrillHead<Array<LinEntry>> {
    val cmd = "LIN-CHOICES"
    override val klass: KClass<Array<LinEntry>> = Array<LinEntry>::class

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
                            OptionalInlineWhitespace(),
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
                                push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChangeUIEntry(18, 4)), "")))
                            },
                            OneOrMore(
                                    Sequence(
                                            OptionalWhitespace(),
                                            ParameterToStack(),
                                            Optional(
                                                    OptionalInlineWhitespace(),
                                                    "->"
                                            ),
                                            Action<Any> {
                                                val name = pop()
                                                val currentChoice = data["$cmd-CHOICE"]?.toString()?.toIntOrNull() ?: 0
                                                data["$cmd-CHOICE"] = currentChoice + 1

                                                push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChoiceEntry(currentChoice + 1)), "")))
                                                push(listOf(SpiralDrillBit(BasicLinTextDrill, ""), name))
                                                push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(when (hopesPeakGame) {
                                                    DR1 -> WaitFrameEntry.DR1
                                                    DR2 -> WaitFrameEntry.DR2
                                                    else -> TODO("Unknown game $hopesPeakGame (Text hasn't been completely documented!)")
                                                }), "")))
                                            },
                                            OptionalInlineWhitespace(),
                                            '{',
                                            '\n',
                                            OpenSpiralLines(),
                                            Action<Any> {
                                                push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(GoToLabelEntry.forGame(drGame, (data["$cmd-LABEL"] as Int))), "")))
                                            },
                                            OptionalWhitespace(),
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinEntry>? {
        val label = rawParams[1].toString().toIntOrNull() ?: parser.findLabel()
        if (rawParams[0].toString().isBlank())
            return arrayOf(ChoiceEntry(18), ChoiceEntry(19), ChoiceEntry(255), SetLabelEntry.forGame(parser.drGame, label))
        return when (parser.hopesPeakGame) {
            DR1 -> arrayOf(
                    ChoiceEntry(18),
                    ChoiceEntry(19),
                    TextEntry(rawParams[0].toString(), -1),
                    WaitFrameEntry.DR1,
                    ChoiceEntry(255),
                    SetLabelEntry.forGame(parser.drGame, label)
            )
            DR2 -> arrayOf(
                    ChoiceEntry(18),
                    ChoiceEntry(19),
                    TextEntry(rawParams[0].toString(), -1),
                    WaitFrameEntry.DR1,
                    ChoiceEntry(255),
                    SetLabelEntry.forGame(parser.drGame, label)
            )
            else -> TODO("Choices are not documented for ${parser.hopesPeakGame}")
        }
    }
}
