package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.drills.StaticDrill
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.osl.drills.headerCircuits.ItemSelectionDrill
import info.spiralframework.formats.scripting.lin.ChangeUIEntry
import info.spiralframework.formats.scripting.lin.ChoiceEntry
import info.spiralframework.formats.common.scripting.lin.LinEntry
import org.parboiled.Action
import org.parboiled.Rule

/** Probably one of the more complicated codes */
object InternalLinItemSelectionDrill : DrillCircuit {
    val DIGITS = "\\d+".toRegex()

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "[Internal] Select ",
                    FirstOf("Present", "Presents", "Item", "Items"),
                    OptionalWhitespace(),
                    '{',
                    '\n',
                    Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChangeUIEntry(19, 1)), ""))) },
                    OneOrMore(
                            FirstOf(
                                    Sequence(
                                            OptionalWhitespace(),
                                            ParameterToStack(),
                                            Action<Any> {
                                                val name = pop().toString().toLowerCase()
                                                return@Action name in ItemSelectionDrill.MISSING_ITEMS
                                            },
                                            Optional(
                                                    OptionalInlineWhitespace(),
                                                    "->"
                                            ),
                                            OptionalInlineWhitespace(),
                                            '{',
                                            '\n',
                                            Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChoiceEntry(252)), ""))) },
                                            OpenSpiralLines(),
                                            OptionalWhitespace(),
                                            '}',
                                            '\n',
                                            OptionalWhitespace()
                                    ),
                                    Sequence(
                                            OptionalWhitespace(),
                                            ParameterToStack(),
                                            Action<Any> {
                                                val name = pop().toString().toLowerCase()
                                                return@Action name in ItemSelectionDrill.ON_EXIT
                                            },
                                            Optional(
                                                    OptionalInlineWhitespace(),
                                                    "->"
                                            ),
                                            OptionalInlineWhitespace(),
                                            '{',
                                            '\n',
                                            Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChoiceEntry(253)), ""))) },
                                            OpenSpiralLines(),
                                            OptionalWhitespace(),
                                            '}',
                                            '\n',
                                            OptionalWhitespace()
                                    ),
                                    Sequence(
                                            OptionalWhitespace(),
                                            ParameterToStack(),
                                            Action<Any> {
                                                val name = pop().toString().toLowerCase()
                                                return@Action name in ItemSelectionDrill.OPENING_LINES
                                            },
                                            Optional(
                                                    OptionalInlineWhitespace(),
                                                    "->"
                                            ),
                                            OptionalInlineWhitespace(),
                                            '{',
                                            '\n',
                                            Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChoiceEntry(254)), ""))) },
                                            OpenSpiralLines(),
                                            OptionalWhitespace(),
                                            '}',
                                            '\n',
                                            OptionalWhitespace()
                                    ),
                                    Sequence(
                                            OptionalWhitespace(),
                                            ItemID(),
                                            Action<Any> {
                                                val id = pop().toString().toIntOrNull() ?: 0
                                                return@Action push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChoiceEntry(id)), "")))
                                            },
                                            Optional(
                                                    OptionalInlineWhitespace(),
                                                    "->"
                                            ),
                                            OptionalInlineWhitespace(),
                                            '{',
                                            '\n',
                                            OpenSpiralLines(),
                                            OptionalWhitespace(),
                                            '}',
                                            '\n',
                                            OptionalWhitespace()
                                    )
                            )
                    ),
                    '}',
                    Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinEntry>(ChoiceEntry(255)), ""))) }
            )
}
