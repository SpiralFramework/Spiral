package info.spiralframework.osl.drills.lin

import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.*
import info.spiralframework.osl.EnumLinFlagCheck
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.osl.drills.headerCircuits.SpiralBridgeDrill
import info.spiralframework.osl.pushStaticDrillDirect
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

object LinIfDrill : DrillCircuit {
    val explicitFlag = arrayOf("iff", "if-f")
    val explicitGameState = arrayOf("g-if", "gameContext-if", "ifg", "if-g")
    val explicitSpiralBridge = arrayOf("if-bridge", "if-spiral", "if-s")

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val ifTrueVar = Var(0)
        val elseVar = Var(0)
        val returnBranchVar = Var(0)

        val flagToCheckVar = Var(mutableListOf(0))
        val mostRecentFlagVar = Var(0)
        val operationVar = Var(mutableListOf(0))
        val valueToCheckVar = Var(mutableListOf(0))
        val joiningOperationVar = Var(mutableListOf<Int>())

        val modeVar = Var(0)

        val flagRule = Sequence(
                Flag(),
                Action<Any> {
                    flagToCheckVar.get().add((pop().toString().toIntOrNull()
                            ?: 0) or ((pop().toString().toIntOrNull()
                            ?: 0) shl 8))

                    mostRecentFlagVar.set(flagToCheckVar.get().last())
                },
                OptionalInlineWhitespace(),

                LinIfOperator(),
                Action<Any> { operationVar.get().add(pop().toString().toIntOrNull() ?: 0) },

                OptionalInlineWhitespace(),
                FlagValue(),
                Action<Any> { valueToCheckVar.get().add(pop().toString().toIntOrNull() ?: 0) }
        )

        val gameStateRule = Sequence(
                GameState(),
                Action<Any> {
                    val state = pop().toString().toIntOrNull() ?: 0
                    flagToCheckVar.get().add(state)
                    mostRecentFlagVar.set(flagToCheckVar.get().last())
                    return@Action true
                },
                OptionalInlineWhitespace(),

                LinIfOperator(),
                Action<Any> { operationVar.get().add(pop().toString().toIntOrNull() ?: 0) },

                OptionalInlineWhitespace(),
                GameStateValue(mostRecentFlagVar),
                Action<Any> { valueToCheckVar.get().add(pop().toString().toIntOrNull() ?: 0) }
        )

        val spiralBridgeRule = Sequence(
                SpiralBridgeName(),
                Action<Any> {
                    val state = pop().toString().toIntOrNull() ?: 0
                    flagToCheckVar.get().add(state)
                    mostRecentFlagVar.set(flagToCheckVar.get().last())
                    return@Action true
                },
                OptionalInlineWhitespace(),
                LinIfOperator(),
                Action<Any> { operationVar.get().add(pop().toString().toIntOrNull() ?: 0) },

                OptionalInlineWhitespace(),
                Action<Any> { push(mostRecentFlagVar.get()) },
                SpiralBridgeValue(),
                Action<Any> { valueToCheckVar.get().add(pop().toString().toIntOrNull() ?: 0) }
        )

        return Sequence(
                Action<Any> {
                    flagToCheckVar.get().clear()
                    operationVar.get().clear()
                    valueToCheckVar.get().clear()
                    joiningOperationVar.get().clear()

                    true
                },
                FirstOf(
                        Sequence(
                                FirstOf(explicitFlag),
                                Action<Any> { modeVar.set(0) }
                        ),
                        Sequence(
                                FirstOf(explicitGameState),
                                Action<Any> { modeVar.set(1) }
                        ),
                        Sequence(
                                FirstOf(explicitSpiralBridge),
                                Action<Any> { modeVar.set(2) }
                        ),
                        Sequence(
                                "if",
                                Action<Any> { modeVar.set(-1) }
                        )
                ),
                OptionalInlineWhitespace(),
                "(",
                OptionalInlineWhitespace(),

                FirstOf(
                        Sequence(
                                Action<Any> { modeVar.get() == 0 || modeVar.get() == -1 },
                                flagRule,
                                ZeroOrMore(
                                        OptionalInlineWhitespace(),
                                        JoinerOperator(),
                                        Action<Any> {
                                            joiningOperationVar.get().add(pop().toString().toIntOrNull() ?: 0)
                                        },
                                        OptionalInlineWhitespace(),
                                        flagRule
                                ),
                                Action<Any> { modeVar.set(0) }
                        ),
                        Sequence(
                                Action<Any> { modeVar.get() == 1 || modeVar.get() == -1 },
                                gameStateRule,
                                ZeroOrMore(
                                        OptionalInlineWhitespace(),
                                        JoinerOperator(),
                                        Action<Any> {
                                            joiningOperationVar.get().add(pop().toString().toIntOrNull() ?: 0)
                                        },
                                        OptionalInlineWhitespace(),
                                        gameStateRule
                                ),
                                Action<Any> { modeVar.set(1) }
                        ),
                        Sequence(
                                Action<Any> { modeVar.get() == 2 || modeVar.get() == -1 },
                                spiralBridgeRule,
                                ZeroOrMore(
                                        OptionalInlineWhitespace(),
                                        JoinerOperator(),
                                        Action<Any> {
                                            joiningOperationVar.get().add(pop().toString().toIntOrNull() ?: 0)
                                        },
                                        OptionalInlineWhitespace(),
                                        spiralBridgeRule
                                ),
                                Action<Any> { flagToCheckVar.get().distinct().size == 1 },
                                Action<Any> { modeVar.set(2) }
                        )
                ),

                OptionalInlineWhitespace(),
                ')',
                OptionalWhitespace(),
                "{",
                '\n',
                Action<Any> {
                    ifTrueVar.set(findLabel())
                    elseVar.set(findLabel())

                    val flagToCheck = flagToCheckVar.get()
                    val operations = operationVar.get()
                    val valueToCheck = valueToCheckVar.get()
                    val joiners = joiningOperationVar.get()

                    when (modeVar.get()) {
                        0 -> pushStaticDrillDirect(when (hopesPeakGame) {
                            DR1 -> arrayOf(
                                    CheckFlagAEntry(0x35, (0 until flagToCheck.size).flatMap { i ->
                                        if (i < flagToCheck.size - 1)
                                            listOf(flagToCheck[i] shr 8, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i], joiners[i])
                                        else
                                            listOf(flagToCheck[i] shr 8, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i])
                                    }.toIntArray()),
                                    EndFlagCheckEntry(),
                                    GoToLabelEntry.DR1(ifTrueVar.get()),
                                    GoToLabelEntry.DR1(elseVar.get()),

                                    SetLabelEntry.DR1(ifTrueVar.get())
                            )
                            DR2 -> arrayOf(
                                    CheckFlagAEntry(0x35, (0 until flagToCheck.size).flatMap { i ->
                                        if (i < flagToCheck.size - 1)
                                            listOf(flagToCheck[i] shr 8, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i], joiners[i])
                                        else
                                            listOf(flagToCheck[i] shr 8, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i])
                                    }.toIntArray()),
                                    EndFlagCheckEntry(),
                                    GoToLabelEntry.DR2(ifTrueVar.get()),
                                    GoToLabelEntry.DR2(elseVar.get()),

                                    SetLabelEntry.DR2(ifTrueVar.get())
                            )
                            else -> TODO("Flag Checks are not documented for ${hopesPeakGame}")
                        })

                        1 -> pushStaticDrillDirect(when (hopesPeakGame) {
                            DR1 -> arrayOf(
                                    UnknownEntry(0x36, (0 until flagToCheck.size).flatMap { i ->
                                        if (i < flagToCheck.size - 1)
                                            listOf(0, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF, joiners[i])
                                        else
                                            listOf(0, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF)
                                    }.toIntArray()),
                                    EndFlagCheckEntry(),
                                    GoToLabelEntry.DR1(ifTrueVar.get()),
                                    GoToLabelEntry.DR1(elseVar.get()),

                                    SetLabelEntry.DR1(ifTrueVar.get())
                            )
                            DR2 -> arrayOf(
                                    UnknownEntry(0x36, (0 until flagToCheck.size).flatMap { i ->
                                        if (i < flagToCheck.size - 1)
                                            listOf(0, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF, joiners[i])
                                        else
                                            listOf(0, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF)
                                    }.toIntArray()),
                                    EndFlagCheckEntry(),
                                    GoToLabelEntry.DR2(ifTrueVar.get()),
                                    GoToLabelEntry.DR2(elseVar.get()),

                                    SetLabelEntry.DR2(ifTrueVar.get())
                            )
                            else -> TODO("Flag Checks are not documented for ${hopesPeakGame}")
                        })

                        2 -> {
                            val ifPartiallyTrue = findLabel()
                            pushStaticDrillDirect(when (hopesPeakGame) {
                                DR1 -> arrayOf(
                                        UnknownEntry(0x36, intArrayOf(0, SpiralBridgeDrill.OP_CODE_GAME_STATE, info.spiralframework.osl.EnumLinFlagCheck.NOT_EQUALS.flag, (flagToCheck[0] shr 8), (flagToCheck[0] and 0xFF))),
                                        EndFlagCheckEntry(),
                                        GoToLabelEntry.DR1(elseVar.get()),
                                        UnknownEntry(0x36, (0 until flagToCheck.size).flatMap { i ->
                                            if (i < flagToCheck.size - 1)
                                                listOf(0, SpiralBridgeDrill.OP_CODE_PARAM_BIG, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF, joiners[i])
                                            else
                                                listOf(0, SpiralBridgeDrill.OP_CODE_PARAM_BIG, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF)
                                        }.toIntArray()),
                                        EndFlagCheckEntry(),
                                        GoToLabelEntry.DR1(ifTrueVar.get()),
                                        GoToLabelEntry.DR1(elseVar.get()),

                                        SetLabelEntry.DR1(ifTrueVar.get())
                                )
                                DR2 -> arrayOf(
                                        UnknownEntry(0x36, (0 until flagToCheck.size).flatMap { i ->
                                            if (i < flagToCheck.size - 1)
                                                listOf(0, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF, joiners[i])
                                            else
                                                listOf(0, flagToCheck[i] and 0xFF, operations[i], valueToCheck[i] shr 8, valueToCheck[i] and 0xFF)
                                        }.toIntArray()),
                                        EndFlagCheckEntry(),
                                        GoToLabelEntry.DR2(ifTrueVar.get()),
                                        GoToLabelEntry.DR2(elseVar.get()),

                                        SetLabelEntry.DR2(ifTrueVar.get())
                                )
                                else -> TODO("Flag Checks are not documented for ${hopesPeakGame}")
                            })
                        }
                    }

                    return@Action true
                },
                OpenSpiralLines(),
                "}",
                Action<Any> { true },
                FirstOf(
                        Sequence(
                                Whitespace(),
                                "else",
                                Whitespace(),
                                "{",
                                "\n",
                                Action<Any> {
                                    returnBranchVar.set(findLabel())

                                    pushStaticDrillDirect(when (hopesPeakGame) {
                                        DR1 -> arrayOf(GoToLabelEntry.DR1(returnBranchVar.get()), SetLabelEntry.DR1(elseVar.get()))
                                        DR2 -> arrayOf(GoToLabelEntry.DR2(returnBranchVar.get()), SetLabelEntry.DR2(elseVar.get()))
                                        else -> TODO("No label support for $hopesPeakGame")
                                    })
                                    return@Action true
                                },
                                OpenSpiralLines(),
                                "}"
                        ),
                        Sequence(
                                Whitespace(),
                                "else",
                                Whitespace(),
                                Action<Any> {
                                    returnBranchVar.set(findLabel())

                                    pushStaticDrillDirect(when (hopesPeakGame) {
                                        DR1 -> arrayOf(GoToLabelEntry.DR1(returnBranchVar.get()), SetLabelEntry.DR1(elseVar.get()))
                                        DR2 -> arrayOf(GoToLabelEntry.DR2(returnBranchVar.get()), SetLabelEntry.DR2(elseVar.get()))
                                        else -> TODO("No label support for $hopesPeakGame")
                                    })
                                    return@Action true
                                },
                                LinIfCommand()
                        ),

                        Action<Any> {
                            pushStaticDrillDirect(when (hopesPeakGame) {
                                DR1 -> SetLabelEntry.DR1(elseVar.get())
                                DR2 -> SetLabelEntry.DR2(elseVar.get())
                                else -> TODO("No label support for $hopesPeakGame")
                            })
                        }
                )
        )
    }
}
