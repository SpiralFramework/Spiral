package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

//While this is an extension
object SpiralBridgeDrill : DrillCircuit {
    /** Do NOT set these to 35, 36, and 37 */
    var OP_CODE_GAME_STATE: Int = 51
    var OP_CODE_PARAM_BIG: Int = 52
    var OP_CODE_PARAM_SMALL: Int = 53

    val OP_CODE_NAMES = mapOf(
            "Noop" to 0,
            "Nop" to 0,
            "No-op" to 0,
            "Pass" to 0,
            "Reset" to 0,

            "Synchronisation" to 1,
            "Synchronization" to 1,
            "Synchronise" to 1,
            "Synchronize" to 1,

            "PrevChoice" to 2,

            "WaitForChoice" to 3,

            "LoadBridgeFile" to 4,

            "RequestAction" to 5,
            "RequestHelp" to 5,
            "RequestDrill" to 5,

            "StoreValue" to 6,

            "StoreGameState" to 7,

            "RestoreGameState" to 8,

            "RunScript" to 9,

            "ServerAcknowledgement" to 128,
            "ServerAck" to 128,

            "ServerKey" to 129,
            "ServerPress" to 129,

            "ServerChoice" to 130
    )

    val OP_CODE_VALUES = mapOf(
            5 to mapOf(
                    "TEXT_BUFFER_CLEAR" to 0
            ),
            7 to mapOf(
                    "TIME_OF_DAY" to 0,
                    "TIME OF DAY" to 0,
                    "TIMEOFDAY" to 0,

                    "LAST_EVIDENCE" to 10,
                    "LAST EVIDENCE" to 10,
                    "EVIDENCE" to 10,

                    "GAMEMODE" to 15,
                    "GAME MODE" to 15,
                    "GAME_MODE" to 15,

                    "ACTION_DIFFICULTY" to 18,
                    "ACTION DIFFICULTY" to 18,
                    "ACTION_DIFF" to 18,
                    "ACTION DIFF" to 18,

                    "LOGIC_DIFFICULTY" to 19,
                    "LOGIC DIFFICULTY" to 19,
                    "LOGIC_DIFF" to 19,
                    "LOGIC DIFF" to 19,
                    "INFERENCE_DIFFICULTY" to 19,
                    "INFERENCE DIFFICULTY" to 19,
                    "INFERENCE_DIFF" to 19,
                    "INFERENCE DIFF" to 19
            ),
            8 to mapOf(
                    "TIME_OF_DAY" to 0,
                    "TIME OF DAY" to 0,
                    "TIMEOFDAY" to 0,

                    "LAST_EVIDENCE" to 10,
                    "LAST EVIDENCE" to 10,
                    "EVIDENCE" to 10,

                    "GAMEMODE" to 15,
                    "GAME MODE" to 15,
                    "GAME_MODE" to 15,

                    "ACTION_DIFFICULTY" to 18,
                    "ACTION DIFFICULTY" to 18,
                    "ACTION_DIFF" to 18,
                    "ACTION DIFF" to 18,

                    "LOGIC_DIFFICULTY" to 19,
                    "LOGIC DIFFICULTY" to 19,
                    "LOGIC_DIFF" to 19,
                    "LOGIC DIFF" to 19,
                    "INFERENCE_DIFFICULTY" to 19,
                    "INFERENCE DIFFICULTY" to 19,
                    "INFERENCE_DIFF" to 19,
                    "INFERENCE DIFF" to 19
            ),
            129 to mapOf(
                    "INPUT" to 0
            )
    )

    val ALL_OP_CODE_VALUES = OP_CODE_VALUES.values.flatMap { map -> map.keys }.distinct().toTypedArray()

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val opCode = Var<Int>(0)
        val value = Var<Int>(0)

        return Sequence(
                Action<Any> {
                    opCode.set(0)
                    value.set(0)
                },

                "SpiralBridge:",
                OptionalInlineWhitespace(),
                FirstOf(
                        Sequence(
                                "clear",
                                Action<Any> {
                                    push(arrayOf(this, "0x33|$OP_CODE_GAME_STATE, 0, 0, 0"))
                                    push(arrayOf(this, "0x33|$OP_CODE_PARAM_BIG, 0, 0, 0"))
                                    push(arrayOf(this, "0x33|$OP_CODE_PARAM_SMALL, 0, 0, 0"))
                                }
                        ),
                        Sequence(
                                SpiralBridgeName(),
                                Action<Any> { opCode.set(pop() as Int) },
                                CommaSeparator(),
                                Action<Any> { push(opCode.get()) },
                                SpiralBridgeValue(),
                                Action<Any> { value.set(pop() as Int) },
                                Action<Any> { push(arrayOf(this, "0x33|$OP_CODE_GAME_STATE, 0, ${(opCode.get() shr 8) and 0xFF}, ${opCode.get() and 0xFF}")) },
                                Action<Any> { push(arrayOf(this, "0x33|$OP_CODE_PARAM_BIG, 0, ${(value.get() shr 24) and 0xFF}, ${(value.get() shr 16) and 0xFF}")) },
                                Action<Any> { push(arrayOf(this, "0x33|$OP_CODE_PARAM_SMALL, 0, ${(value.get() shr 8) and 0xFF}, ${(value.get() shr 0) and 0xFF}")) }
                        )
                )
        )
    }
}