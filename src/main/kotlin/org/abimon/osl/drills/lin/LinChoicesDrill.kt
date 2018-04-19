package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillException
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
                    "Display Choices",
                    Action<Any> {
                        if (cmd in tmpStack)
                            throw SpiralDrillException("Nested choice selection detected; this is unsupported! If you wish to nest choices, you will need to use labels.")
                        return@Action true
                    },
                    pushTmpAction(cmd, this@LinChoicesDrill),
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
                        data["$cmd-LABEL"] = labels++
                        push(listOf(StaticDrill<LinScript>(ChangeUIEntry(18, 4))))
                    },
                    OneOrMore(
                            Sequence(
                                    OptionalWhitespace(),
                                    ParameterToStack(),
                                    Action<Any> {
                                        val name = pop()
                                        val currentChoice = data["$cmd-CHOICE"]?.toString()?.toIntOrNull() ?: 0
                                        data["$cmd-CHOICE"] = currentChoice + 1

                                        push(listOf(StaticDrill<LinScript>(ChoiceEntry(currentChoice + 1))))
                                        push(listOf(BasicLinTextDrill, name))
                                        push(listOf(StaticDrill<LinScript>(when (game) {
                                            DR1 -> WaitFrameEntry.DR1
                                            DR2 -> WaitFrameEntry.DR2
                                            else -> TODO("Unknown game $game (Text hasn't been completely documented!)")
                                        })))
                                    },
                                    OptionalWhitespace(),
                                    '{',
                                    '\n',
                                    OpenSpiralLines(),
                                    Action<Any> {
                                        push(listOf(StaticDrill<LinScript>(GoToLabelEntry((128 * 256) + (data["$cmd-LABEL"] as? Int
                                                ?: 0)))))
                                    },
                                    '}',
                                    '\n'
                            )
                    ),
                    '}',

                    Action<Any> {
                        pushTmp(cmd, data["$cmd-LABEL"] as? Int ?: 0)

                        this["$cmd-CHOICE"] = null
                        this["$cmd-LABEL"] = null

                        return@Action true
                    },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript>? {
        println(rawParams.joinToString { "[$it]" })
        if (rawParams[0].toString().isBlank())
            return arrayOf(ChoiceEntry(18), ChoiceEntry(19), ChoiceEntry(255), SetLabelEntry((128 * 256) + (rawParams[1].toString().toIntOrNull() ?: 0)))
        return arrayOf(
                ChoiceEntry(18),
                ChoiceEntry(19),
                TextEntry(rawParams[0].toString(), -1),
                when (parser.game) {
                    DR1 -> WaitFrameEntry.DR1
                    DR2 -> WaitFrameEntry.DR2
                    else -> TODO("Unknown game ${parser.game} (Text hasn't been completely documented!)")
                },
                ChoiceEntry(255),
                SetLabelEntry((128 * 256) + (rawParams[1].toString().toIntOrNull() ?: 0))
        )
    }
}