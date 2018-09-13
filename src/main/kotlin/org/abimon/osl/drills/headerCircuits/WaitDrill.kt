package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var

object WaitDrill : DrillCircuit {
    val ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray()

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    FirstOf("Wait for", "Wait until"),
                    InlineWhitespace(),
                    FirstOf(
                            XFrames(),
                            GameStateChange(),
                            SpiralBridge()
                    )
            )

    fun OpenSpiralLanguageParser.XFrames(): Rule =
            Sequence(
                    FrameCount(),
                    Action<Any> { context ->
                        for (i in 0 until (pop().toString().toIntOrNull() ?: 0)) {
                            push(arrayOf(this, "Wait Frame|"))
                        }

                        return@Action true
                    }
            )

    fun OpenSpiralLanguageParser.GameStateChange(): Rule {
        val gameState = Var<Int>(0)
        val desiredGameState = Var<Int>(0)
        val waitingSleep = Var<Int>(0)
        val waitingText = Var<String>("")

        return Sequence(
                Action<Any> {
                    gameState.set(0)
                    desiredGameState.set(0)
                    waitingSleep.set(OpenSpiralLanguageParser.FRAMES_PER_SECOND)
                    waitingText.set("")
                },
                "Game State",
                InlineWhitespace(),
                GameState(),
                Action<Any> { gameState.set(pop().toString().toIntOrNull() ?: 0) },
                FirstOf(
                        Sequence(
                                InlineWhitespace(),
                                FirstOf("is", "to be"),
                                InlineWhitespace(),
                                RuleWithVariables(OneOrMore(Digit())),
                                Action<Any> { desiredGameState.set(pop().toString().toIntOrNull() ?: 0) },
                                Optional(
                                        OptionalInlineWhitespace(),
                                        "wait for",
                                        OptionalInlineWhitespace(),
                                        FrameCount(),
                                        Optional(
                                                OptionalInlineWhitespace(),
                                                "between loops"
                                        ),
                                        Action<Any> { waitingSleep.set(pop().toString().toIntOrNull() ?: 0) }
                                )
                        ),
                        Sequence(
                                OptionalInlineWhitespace(),
                                '(',
                                OptionalInlineWhitespace(),
                                RuleWithVariables(OneOrMore(Digit())),
                                Action<Any> { desiredGameState.set(pop().toString().toIntOrNull() ?: 0) },
                                OptionalInlineWhitespace(),
                                Optional(
                                        ',',
                                        OptionalInlineWhitespace(),
                                        RuleWithVariables(OneOrMore(Digit())),
                                        Action<Any> { waitingSleep.set(pop().toString().toIntOrNull() ?: 0) },
                                        OptionalInlineWhitespace()
                                ),
                                ')'
                        ),
                        Sequence(
                                OptionalInlineWhitespace(),
                                '[',
                                OptionalInlineWhitespace(),
                                RuleWithVariables(OneOrMore(Digit())),
                                OptionalInlineWhitespace(),
                                Optional(
                                        ',',
                                        OptionalInlineWhitespace(),
                                        RuleWithVariables(OneOrMore(Digit())),
                                        Action<Any> { waitingSleep.set(pop().toString().toIntOrNull() ?: 0) },
                                        OptionalInlineWhitespace()
                                ),
                                ']',
                                Action<Any> { desiredGameState.set(pop().toString().toIntOrNull() ?: 0) }
                        )
                ),
                OptionalInlineWhitespace(),
                Optional(
                        OptionalWhitespace(),
                        '{',
                        OptionalInlineWhitespace(),
                        '\n',
                        saveState(),
                        OpenSpiralHeaderLines(),
                        Action<Any> { context ->
                            loadState(context)
                            waitingText.set(context.match)
                        },
                        OptionalWhitespace(),
                        "}"
                ),

                Action<Any> {
                    val label = findLabel()

                    push(arrayOf(this, "Mark Label $label"))

                    push(arrayOf(this, "if-g (${gameState.get()} != ${desiredGameState.get()}) {"))

                    for (i in 0 until waitingSleep.get()) {
                        push(arrayOf(this, "Wait Frame|"))
                    }

                    push(arrayOf(this, "goto $label"))
                    push(arrayOf(this, "}"))

                    return@Action true
                }
        )
    }

    fun OpenSpiralLanguageParser.SpiralBridge(): Rule {
        val spiralOp = Var<Int>(0)
        val desiredGameState = Var<Int>(-1)
        val waitingSleep = Var<Int>(0)
        val waitingText = Var<String>("")

        return Sequence(
                Action<Any> {
                    desiredGameState.set(-1)
                    waitingSleep.set(OpenSpiralLanguageParser.FRAMES_PER_SECOND)
                    waitingText.set("")
                },
                "SpiralBridge",
                InlineWhitespace(),
                FirstOf(
                        Sequence(
                                "0x",
                                OneOrMore(Digit(16)),
                                Action<Any> {
                                    spiralOp.set(match().toIntOrNull(16) ?: 0)
                                }
                        ),
                        Sequence(
                                OneOrMore(Digit(10)),
                                Action<Any> {
                                    spiralOp.set(match().toIntOrNull(16) ?: 0)
                                }
                        ),
                        Sequence(
                                FirstOf(SpiralBridgeDrill.OP_CODE_NAMES.keys.toTypedArray()),
                                Action<Any> {
                                    spiralOp.set(SpiralBridgeDrill.OP_CODE_NAMES[match()] ?: 0)
                                }
                        )
                ),
                Optional(
                        FirstOf(
                                Sequence(
                                        InlineWhitespace(),
                                        FirstOf("is", "to be"),
                                        InlineWhitespace(),
                                        SpiralBridgeValue(spiralOp, desiredGameState),
                                        Optional(
                                                OptionalInlineWhitespace(),
                                                "wait for",
                                                OptionalInlineWhitespace(),
                                                FrameCount(),
                                                Optional(
                                                        OptionalInlineWhitespace(),
                                                        "between loops"
                                                ),
                                                Action<Any> { waitingSleep.set(pop().toString().toIntOrNull() ?: 0) }
                                        )
                                ),
                                Sequence(
                                        OptionalInlineWhitespace(),
                                        '(',
                                        OptionalInlineWhitespace(),
                                        SpiralBridgeValue(spiralOp, desiredGameState),
                                        OptionalInlineWhitespace(),
                                        Optional(
                                                ',',
                                                OptionalInlineWhitespace(),
                                                RuleWithVariables(OneOrMore(Digit())),
                                                Action<Any> { waitingSleep.set(pop().toString().toIntOrNull() ?: 0) },
                                                OptionalInlineWhitespace()
                                        ),
                                        ')'
                                ),
                                Sequence(
                                        OptionalInlineWhitespace(),
                                        '[',
                                        OptionalInlineWhitespace(),
                                        SpiralBridgeValue(spiralOp, desiredGameState),
                                        OptionalInlineWhitespace(),
                                        Optional(
                                                ',',
                                                OptionalInlineWhitespace(),
                                                RuleWithVariables(OneOrMore(Digit())),
                                                Action<Any> { waitingSleep.set(pop().toString().toIntOrNull() ?: 0) },
                                                OptionalInlineWhitespace()
                                        ),
                                        ']',
                                        Action<Any> { desiredGameState.set(pop().toString().toIntOrNull() ?: 0) }
                                )
                        )
                ),
                OptionalInlineWhitespace(),
                Optional(
                        OptionalWhitespace(),
                        '{',
                        OptionalInlineWhitespace(),
                        '\n',
                        saveState(),
                        OpenSpiralHeaderLines(),
                        Action<Any> { context ->
                            loadState(context)
                            waitingText.set(context.match)
                        },
                        OptionalWhitespace(),
                        "}"
                ),

                Action<Any> {
                    val label = findLabel()

                    push(arrayOf(this, "0x33|28, 0, 0, 0"))
                    push(arrayOf(this, "0x33|29, 0, 0, 0"))
                    push(arrayOf(this, "0x33|30, 0, 0, 0"))
                    push(arrayOf(this, "Mark Label $label"))

                    push(arrayOf(this, "if-g (28 != ${spiralOp.get()}) {"))
                    for (i in 0 until waitingSleep.get()) {
                        push(arrayOf(this, "Wait Frame|"))
                    }
                    push(arrayOf(this, "goto $label"))
                    push(arrayOf(this, "}"))

                    //Check specific
                    if (desiredGameState.get() != -1) {
                        val big = desiredGameState.get() shr 8
                        val small = desiredGameState.get() and 0xFF

                        push(arrayOf(this, "if-g (29 != $big) {"))
                        for (i in 0 until waitingSleep.get()) {
                            push(arrayOf(this, "Wait Frame|"))
                        }
                        push(arrayOf(this, "goto $label"))
                        push(arrayOf(this, "}"))

                        push(arrayOf(this, "if-g (30 != $small) {"))
                        for (i in 0 until waitingSleep.get()) {
                            push(arrayOf(this, "Wait Frame|"))
                        }
                        push(arrayOf(this, "goto $label"))
                        push(arrayOf(this, "}"))
                    }

                    return@Action true
                }
        )
    }

    fun OpenSpiralLanguageParser.SpiralBridgeValue(opCode: Var<Int>, desiredGameState: Var<Int>): Rule =
            FirstOf(
                    Sequence(
                            FirstOf(SpiralBridgeDrill.ALL_OP_CODE_VALUES),
                            Action<Any> {
                                val num = (SpiralBridgeDrill.OP_CODE_VALUES[opCode.get()] ?: return@Action false)[match()]
                                        ?: return@Action false
                                desiredGameState.set(num)
                            }
                    ),
                    Sequence(
                            RuleWithVariables(OneOrMore(Digit())),

                            OptionalInlineWhitespace(),
                            ',',
                            OptionalInlineWhitespace(),

                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                val small = pop().toString().toIntOrNull() ?: 0
                                val big = pop().toString().toIntOrNull() ?: 0
                                desiredGameState.set((big shl 8) or small)
                            }
                    ),
                    Sequence(
                            RuleWithVariables(OneOrMore(Digit())),
                            Action<Any> {
                                desiredGameState.set(pop().toString().toIntOrNull() ?: 0)
                            }
                    )
            )

    fun Int.toAlphaString(radixO: Int): String {
        var i = this
        var radix = radixO
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10

        val buf = CharArray(33)
        val negative = i < 0
        var charPos = 32

        if (!negative) {
            i = -i
        }

        while (i <= -radix) {
            buf[charPos--] = ALPHABET[-(i % radix)]
            i /= radix
        }
        buf[charPos] = ALPHABET[-i]

        if (negative) {
            buf[--charPos] = '-'
        }

        return String(buf, charPos, 33 - charPos)
    }
}