package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

//While this is an extension
object SpiralBridgeDrill : DrillCircuit {
    val OP_CODE_NAMES = mapOf(
            "Synchronisation" to 0,
            "Synchronization" to 0,
            "Synchronise" to 0,
            "Synchronize" to 0,
            "PrevChoice" to 1,

            "ServerAcknowledgement" to 128,
            "ServerAck" to 128,
            "ServerKey" to 129,
            "ServerPress" to 129
    )

    val OP_CODE_VALUES = mapOf(
            129 to mapOf(
                    "INPUT" to 0
            )
    )

    val ALL_OP_CODE_VALUES = OP_CODE_VALUES.values.flatMap { map -> map.keys }.distinct().toTypedArray()

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val opCode = Var<Int>(0)
        val valueBig = Var<Int>(0)
        val valueSmall = Var<Int>(0)

        return Sequence(
                Sequence(
                        Action<Any> {
                            opCode.set(0)
                            valueBig.set(0)
                            valueSmall.set(0)
                        },

                        "SpiralBridge:",
                        OptionalInlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        "0x",
                                        OneOrMore(Digit(16)),
                                        Action<Any> {
                                            opCode.set(match().toIntOrNull(16) ?: 0)
                                        }
                                ),
                                Sequence(
                                        OneOrMore(Digit(10)),
                                        Action<Any> {
                                            opCode.set(match().toIntOrNull(16) ?: 0)
                                        }
                                ),
                                Sequence(
                                        FirstOf(OP_CODE_NAMES.keys.toTypedArray()),
                                        Action<Any> {
                                            opCode.set(OP_CODE_NAMES[match()] ?: 0)
                                        }
                                )
                        ),
                        CommaSeparator(),
                        FirstOf(
                                Sequence(
                                        FirstOf(ALL_OP_CODE_VALUES),
                                        Action<Any> {
                                            val num = (OP_CODE_VALUES[opCode.get()] ?: return@Action false)[match()]
                                                    ?: return@Action false
                                            valueBig.set(num shr 8)
                                            valueSmall.set(num and 0xFF)
                                        }
                                ),
                                Sequence(
                                        RuleWithVariables(OneOrMore(Digit())),

                                        OptionalInlineWhitespace(),
                                        ',',
                                        OptionalInlineWhitespace(),

                                        RuleWithVariables(OneOrMore(Digit())),
                                        Action<Any> {
                                            valueSmall.set(pop().toString().toIntOrNull() ?: 0)
                                            valueBig.set(pop().toString().toIntOrNull() ?: 0)
                                        }
                                ),
                                Sequence(
                                        RuleWithVariables(OneOrMore(Digit())),
                                        Action<Any> {
                                            val id = pop().toString().toIntOrNull() ?: return@Action false

                                            valueBig.set(id shr 8)
                                            valueSmall.set(id and 0xFF)
                                        }
                                )
                        )
                ),
                Action<Any> { push(arrayOf(this, "0x33|${opCode.get() and 0xFF}, 0, ${valueBig.get() and 0xFF}, ${valueSmall.get() and 0xFF}")) }
        )
    }
}