package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.*
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

/** This may be easier to do with 0x33's op code 14 */
object LinRandChoicesDrill : DrillHead<Array<LinScript>> {
    val cmd = "LIN-RANDOM-CHOICES"
    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    //"for (i in 0 until 10) {",
                    "for-rand",
                    OptionalInlineWhitespace(),
                    "(",
                    FirstOf(
                            Sequence(
                                    ParameterToStack(),
                                    InlineWhitespace(),
                                    "in",
                                    InlineWhitespace(),
                                    FirstOf(
                                            Sequence(
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    InlineWhitespace(),
                                                    FirstOf("until", "downTo", "down_to"),
                                                    InlineWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit()))
                                            ),
                                            Sequence(
                                                    "range",
                                                    OptionalInlineWhitespace(),
                                                    '(',
                                                    OptionalInlineWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    OptionalInlineWhitespace(),
                                                    ',',
                                                    OptionalInlineWhitespace(),
                                                    RuleWithVariables(OneOrMore(Digit())),
                                                    OptionalInlineWhitespace(),
                                                    ')'
                                            )
                                    )
                            ),
                            Sequence(
                                    Optional("int", OptionalInlineWhitespace()),
                                    ParameterToStack(),
                                    OptionalInlineWhitespace(),
                                    "=",
                                    OptionalInlineWhitespace(),
                                    RuleWithVariables(OneOrMore(Digit())),
                                    OptionalInlineWhitespace(),
                                    ';',
                                    OptionalInlineWhitespace(),
                                    ParameterToStack(),
                                    Action<Any> {
                                        val current = pop()
                                        val original = peek(1)

                                        return@Action current == original
                                    },
                                    OptionalInlineWhitespace(),
                                    FirstOf("<", ">"),
                                    OptionalInlineWhitespace(),
                                    RuleWithVariables(OneOrMore(Digit())),
                                    OptionalInlineWhitespace(),
                                    Optional(
                                            ';',
                                            OptionalInlineWhitespace(),
                                            ParameterToStack(),
                                            Action<Any> {
                                                val current = pop()
                                                val original = peek(2)

                                                return@Action current == original
                                            },
                                            OptionalInlineWhitespace(),
                                            FirstOf(
                                                    "++",
                                                    "--",
                                                    Sequence(
                                                            "+=",
                                                            OptionalInlineWhitespace(),
                                                            RuleWithVariables(OneOrMore(Digit()))
                                                    ),
                                                    Sequence(
                                                            "-=",
                                                            OptionalInlineWhitespace(),
                                                            RuleWithVariables(OneOrMore(Digit()))
                                                    )
                                            )
                                    )
                            )
                    ),
                    OptionalInlineWhitespace(),
                    ')',
                    OptionalWhitespace(),
                    '{',
                    '\n',
                    Action<Any> {
                        this[peek(2).toString()] = 0

                        return@Action true
                    },
                    Sequence(
                            saveState(),
                            OpenSpiralLines(),
                            Action<Any> { context ->
                                loadState(context)

                                val limit = pop().toString().toInt()
                                val start = pop().toString().toInt()
                                val variableName = pop().toString()

                                val groups: MutableList<Array<Any>> = ArrayList()

                                for (i in (if (start < limit) start until limit else start downTo limit)) {
                                    val parser = this.copy()
                                    parser[variableName] = groups.size
                                    val result = parser.parse("OSL Script\n${context.match}")
                                    if (!result.hasErrors()) {
                                        groups.add(result.valueStack.reversed().toTypedArray())

                                        labels.clear()
                                        labels.addAll(parser.labels.toTypedArray())
                                    }
                                }

                                //We need to balance out x groups

                                when (groups.size) {
                                    1 -> { //Only run 50% of the time
                                        val ifTrue = findLabel()
                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(ifTrue),
                                                GoToLabelEntry(join),

                                                SetLabelEntry(ifTrue)
                                        ))))

                                        groups[0].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinScript>(SetLabelEntry(join))))
                                    }
                                    2 -> {
                                        val ifTrue = findLabel()
                                        val ifFalse = findLabel()
                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(ifTrue),
                                                GoToLabelEntry(ifFalse),

                                                SetLabelEntry(ifTrue)
                                        ))))

                                        groups[0].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                GoToLabelEntry(join),
                                                SetLabelEntry(ifFalse)
                                        ))))

                                        groups[1].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinScript>(SetLabelEntry(join))))
                                    }
                                    3 -> {
                                        val loop = findLabel()
                                        val firstIf = findLabel()
                                        val secondIf = findLabel()

                                        val labels = IntArray(3) { findLabel() }

                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                SetLabelEntry(loop),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(firstIf),
                                                GoToLabelEntry(secondIf),

                                                SetLabelEntry(firstIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(labels[0]),
                                                GoToLabelEntry(labels[1]),

                                                SetLabelEntry(secondIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(labels[2]),
                                                GoToLabelEntry(loop),
                                                SetLabelEntry(labels[0])
                                        ))))

                                        for (i in 0 until labels.size - 1) {
                                            groups[i].forEach(this::pushValue)

                                            push(listOf(SpiralDrillBit(arrayOf(
                                                    GoToLabelEntry(join),
                                                    SetLabelEntry(labels[i + 1])
                                            ))))
                                        }

                                        groups[2].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinScript>(SetLabelEntry(join))))
                                    }
                                    4 -> {
                                        val firstIf = findLabel()
                                        val secondIf = findLabel()

                                        val labels = IntArray(4) { findLabel() }

                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(firstIf),
                                                GoToLabelEntry(secondIf),

                                                SetLabelEntry(firstIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(labels[0]),
                                                GoToLabelEntry(labels[1]),

                                                SetLabelEntry(secondIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(labels[2]),
                                                GoToLabelEntry(labels[3]),
                                                SetLabelEntry(labels[0])
                                        ))))

                                        for (i in 0 until labels.size - 1) {
                                            groups[i].forEach(this::pushValue)

                                            push(listOf(SpiralDrillBit(arrayOf(
                                                    GoToLabelEntry(join),
                                                    SetLabelEntry(labels[i + 1])
                                            ))))
                                        }

                                        groups[3].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinScript>(SetLabelEntry(join))))
                                    }
                                    5 -> {
                                        val loop = findLabel()
                                        val ifChecks = IntArray(5) { findLabel() }
                                        val labels = IntArray(5) { findLabel() }
                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                SetLabelEntry(loop),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(ifChecks[0]),
                                                GoToLabelEntry(ifChecks[1]),

                                                SetLabelEntry(ifChecks[0]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(ifChecks[2]),
                                                GoToLabelEntry(ifChecks[3]),
                                                
                                                SetLabelEntry(ifChecks[2]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(labels[0]),
                                                GoToLabelEntry(labels[1]),

                                                SetLabelEntry(ifChecks[3]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(labels[2]),
                                                GoToLabelEntry(labels[3]),

                                                SetLabelEntry(ifChecks[1]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(ifChecks[4]),
                                                GoToLabelEntry(loop),

                                                SetLabelEntry(ifChecks[4]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry(labels[4]),
                                                GoToLabelEntry(loop),

                                                SetLabelEntry(labels[0])
                                        ))))

                                        for (i in 0 until labels.size - 1) {
                                            groups[i].forEach(this::pushValue)

                                            push(listOf(SpiralDrillBit(arrayOf(
                                                    GoToLabelEntry(join),
                                                    SetLabelEntry(labels[i + 1])
                                            ))))
                                        }

                                        groups[4].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinScript>(SetLabelEntry(join))))
                                    }
                                }

                                return@Action true
                            }
                    ),
                    "}"
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
    
    fun RandomFlag() = UnknownEntry(0x36, intArrayOf(0, 14, 2, 0, 50))
}