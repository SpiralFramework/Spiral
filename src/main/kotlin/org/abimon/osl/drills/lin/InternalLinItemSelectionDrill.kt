package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.drills.StaticDrill
import org.abimon.osl.drills.circuits.DrillCircuit
import org.abimon.osl.drills.headerCircuits.ItemSelectionDrill
import org.abimon.spiral.core.objects.scripting.lin.ChangeUIEntry
import org.abimon.spiral.core.objects.scripting.lin.ChoiceEntry
import org.abimon.spiral.core.objects.scripting.lin.LinScript
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
                    Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChangeUIEntry(19, 1)), ""))) },
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
                                                    OptionalWhitespace(),
                                                    "->"
                                            ),
                                            OptionalWhitespace(),
                                            '{',
                                            '\n',
                                            Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChoiceEntry(252)), ""))) },
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
                                                    OptionalWhitespace(),
                                                    "->"
                                            ),
                                            OptionalWhitespace(),
                                            '{',
                                            '\n',
                                            Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChoiceEntry(253)), ""))) },
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
                                                    OptionalWhitespace(),
                                                    "->"
                                            ),
                                            OptionalWhitespace(),
                                            '{',
                                            '\n',
                                            Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChoiceEntry(254)), ""))) },
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
                                                return@Action push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChoiceEntry(id)), "")))
                                            },
                                            Optional(
                                                    OptionalWhitespace(),
                                                    "->"
                                            ),
                                            OptionalWhitespace(),
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
                    Action<Any> { push(listOf(SpiralDrillBit(StaticDrill<LinScript>(ChoiceEntry(255)), ""))) }
            )
}