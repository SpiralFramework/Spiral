package info.spiralframework.osl.drills.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.*
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

/** This may be easier to do with 0x33's op code 14 */
object LinRandChoicesDrill : DrillHead<Array<LinEntry>> {
    val cmd = "LIN-RANDOM-CHOICES"
    override val klass: KClass<Array<LinEntry>> = Array<LinEntry>::class

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
                                                GoToLabelEntry.forGame(hopesPeakGame, ifTrue),
                                                GoToLabelEntry.forGame(hopesPeakGame, join),

                                                SetLabelEntry.forGame(drGame, ifTrue)
                                        ))))

                                        groups[0].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinEntry>(SetLabelEntry.forGame(drGame, join))))
                                    }
                                    2 -> {
                                        val ifTrue = findLabel()
                                        val ifFalse = findLabel()
                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, ifTrue),
                                                GoToLabelEntry.forGame(hopesPeakGame, ifFalse),

                                                SetLabelEntry.forGame(drGame, ifTrue)
                                        ))))

                                        groups[0].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                GoToLabelEntry.forGame(hopesPeakGame, join),
                                                SetLabelEntry.forGame(drGame, ifFalse)
                                        ))))

                                        groups[1].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinEntry>(SetLabelEntry.forGame(drGame, join))))
                                    }
                                    3 -> {
                                        val loop = findLabel()
                                        val firstIf = findLabel()
                                        val secondIf = findLabel()

                                        val labels = IntArray(3) { findLabel() }

                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                SetLabelEntry.forGame(drGame, loop),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, firstIf),
                                                GoToLabelEntry.forGame(hopesPeakGame, secondIf),

                                                SetLabelEntry.forGame(drGame, firstIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[0]),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[1]),

                                                SetLabelEntry.forGame(drGame, secondIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[2]),
                                                GoToLabelEntry.forGame(hopesPeakGame, loop),
                                                SetLabelEntry.forGame(drGame, labels[0])
                                        ))))

                                        for (i in 0 until labels.size - 1) {
                                            groups[i].forEach(this::pushValue)

                                            push(listOf(SpiralDrillBit(arrayOf(
                                                    GoToLabelEntry.forGame(hopesPeakGame, join),
                                                    SetLabelEntry.forGame(drGame, labels[i + 1])
                                            ))))
                                        }

                                        groups[2].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinEntry>(SetLabelEntry.forGame(drGame, join))))
                                    }
                                    4 -> {
                                        val firstIf = findLabel()
                                        val secondIf = findLabel()

                                        val labels = IntArray(4) { findLabel() }

                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, firstIf),
                                                GoToLabelEntry.forGame(hopesPeakGame, secondIf),

                                                SetLabelEntry.forGame(drGame, firstIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[0]),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[1]),

                                                SetLabelEntry.forGame(drGame, secondIf),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[2]),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[3]),
                                                SetLabelEntry.forGame(drGame, labels[0])
                                        ))))

                                        for (i in 0 until labels.size - 1) {
                                            groups[i].forEach(this::pushValue)

                                            push(listOf(SpiralDrillBit(arrayOf(
                                                    GoToLabelEntry.forGame(hopesPeakGame, join),
                                                    SetLabelEntry.forGame(drGame, labels[i + 1])
                                            ))))
                                        }

                                        groups[3].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinEntry>(SetLabelEntry.forGame(drGame, join))))
                                    }
                                    5 -> {
                                        val loop = findLabel()
                                        val ifChecks = IntArray(5) { findLabel() }
                                        val labels = IntArray(5) { findLabel() }
                                        val join = findLabel()

                                        push(listOf(SpiralDrillBit(arrayOf(
                                                SetLabelEntry.forGame(drGame, loop),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, ifChecks[0]),
                                                GoToLabelEntry.forGame(hopesPeakGame, ifChecks[1]),

                                                SetLabelEntry.forGame(drGame, ifChecks[0]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, ifChecks[2]),
                                                GoToLabelEntry.forGame(hopesPeakGame, ifChecks[3]),
                                                
                                                SetLabelEntry.forGame(drGame, ifChecks[2]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[0]),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[1]),

                                                SetLabelEntry.forGame(drGame, ifChecks[3]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[2]),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[3]),

                                                SetLabelEntry.forGame(drGame, ifChecks[1]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, ifChecks[4]),
                                                GoToLabelEntry.forGame(hopesPeakGame, loop),

                                                SetLabelEntry.forGame(drGame, ifChecks[4]),
                                                RandomFlag(),
                                                EndFlagCheckEntry(),
                                                GoToLabelEntry.forGame(hopesPeakGame, labels[4]),
                                                GoToLabelEntry.forGame(hopesPeakGame, loop),

                                                SetLabelEntry.forGame(drGame, labels[0])
                                        ))))

                                        for (i in 0 until labels.size - 1) {
                                            groups[i].forEach(this::pushValue)

                                            push(listOf(SpiralDrillBit(arrayOf(
                                                    GoToLabelEntry.forGame(hopesPeakGame, join),
                                                    SetLabelEntry.forGame(drGame, labels[i + 1])
                                            ))))
                                        }

                                        groups[4].forEach(this::pushValue)

                                        push(listOf(SpiralDrillBit<LinEntry>(SetLabelEntry.forGame(drGame, join))))
                                    }
                                }

                                return@Action true
                            }
                    ),
                    "}"
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
                    SetLabelEntry.DR1(label)
            )
            DR2 -> arrayOf(
                    ChoiceEntry(18),
                    ChoiceEntry(19),
                    TextEntry(rawParams[0].toString(), -1),
                    WaitFrameEntry.DR1,
                    ChoiceEntry(255),
                    SetLabelEntry.DR2(label)
            )
            else -> TODO("Choices are not documented for ${parser.hopesPeakGame}")
        }
    }
    
    fun RandomFlag() = UnknownEntry(0x36, intArrayOf(0, 14, 2, 0, 50))
}
