package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.contextFunc
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.SpriteEntry
import org.parboiled.Action
import org.parboiled.BaseParser.EMPTY
import org.parboiled.Rule
import org.parboiled.support.Var
import kotlin.reflect.KClass

object LinSpriteDrill : DrillHead<LinEntry> {
    val cmd: String = "LIN-SPRITE"
    val NAME = info.spiralframework.osl.AllButMatcher(charArrayOf(':', '\n'))
    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinEntry> = LinEntry::class

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val character = Var<Int>()
        val sprite = Var<Int>()
        val position = Var<Int>()

        val state = Var<Int>()
        val transition = Var<Int>()

        return Sequence(
                clearTmpStack(cmd),

                FirstOf(
                        Sequence(
                                FirstOf(
                                        Sequence(
                                                "Display sprite for ",
                                                pushDrillHead(cmd, this@LinSpriteDrill),
                                                SpeakerName(),
                                                Action<Any> {
                                                    val speaker = pop().toString().toIntOrNull() ?: 0

                                                    pushTmp(cmd, speaker)
                                                    character.set(speaker)
                                                },
                                                " with ID ",
                                                SpriteEmotion(character),
                                                pushTmpFromStack(cmd)
                                        ),
                                        Sequence(
                                                "Display",
                                                pushDrillHead(cmd, this@LinSpriteDrill),
                                                InlineWhitespace(),
                                                SpeakerName(),
                                                Action<Any> {
                                                    val speaker = pop().toString().toIntOrNull() ?: 0

                                                    pushTmp(cmd, speaker)
                                                    character.set(speaker)
                                                },
                                                FirstOf(
                                                        CommaSeparator(),
                                                        InlineWhitespace()
                                                ),
                                                "pose",
                                                InlineWhitespace(),
                                                SpriteEmotion(character),
                                                pushTmpFromStack(cmd)
                                        )
                                ),

                                FirstOf(
                                        Sequence(
                                                CommaSeparator(),
                                                Optional(
                                                        "and",
                                                        InlineWhitespace()
                                                )
                                        ),
                                        InlineWhitespace(),
                                        EMPTY
                                ),

                                FirstOf(
                                        Sequence(
                                                OptionalInlineWhitespace(),
                                                Optional("in", InlineWhitespace()),
                                                "position",
                                                InlineWhitespace(),
                                                RuleWithVariables(OneOrMore(Digit())),
                                                pushTmpFromStack(cmd)
                                        ),
                                        pushTmpAction(cmd, 0)
                                ),

                                FirstOf(
                                        Sequence(
                                                CommaSeparator(),
                                                Optional(
                                                        "and",
                                                        InlineWhitespace()
                                                )
                                        ),
                                        InlineWhitespace(),
                                        EMPTY
                                ),

                                FirstOf(
                                        Sequence(
                                                OptionalInlineWhitespace(),
                                                Optional("with", InlineWhitespace()),
                                                "state",
                                                InlineWhitespace(),
                                                RuleWithVariables(OneOrMore(Digit())),
                                                pushTmpFromStack(cmd)
                                        ),
                                        pushTmpAction(cmd, 1)
                                ),

                                FirstOf(
                                        Sequence(
                                                CommaSeparator(),
                                                Optional(
                                                        "and",
                                                        InlineWhitespace()
                                                )
                                        ),
                                        InlineWhitespace(),
                                        EMPTY
                                ),

                                FirstOf(
                                        Sequence(
                                                OptionalInlineWhitespace(),
                                                Optional("with", InlineWhitespace()),
                                                "transition",
                                                InlineWhitespace(),
                                                RuleWithVariables(OneOrMore(Digit())),
                                                pushTmpFromStack(cmd)
                                        ),
                                        pushTmpAction(cmd, 2)
                                )
                        ),
                        Sequence(
                                Action<Any> {
                                    character.set(0)
                                    sprite.set(0)
                                    position.set(0)

                                    state.set(0)
                                    transition.set(0)
                                },
                                "displaySprite",
                                FunctionRule(
                                        arrayOf(
                                                arrayOf("character", "char") to Sequence(
                                                        ParameterToStack(),
                                                        Action<Any> {
                                                            val name = pop().toString()
                                                            val characterID = customIdentifiers[name] ?: (hopesPeakGame
                                                                    ?: UnknownHopesPeakGame).characterIdentifiers[name]
                                                            ?: name.toIntOrNull() ?: return@Action false

                                                            return@Action character.set(characterID)
                                                        }
                                                ),
                                                "sprite" to Sequence(
                                                        SpriteEmotion(character),
                                                        Action<Any> { sprite.set(pop().toString().toIntOrNull() ?: 0) }
                                                ),
                                                arrayOf("position", "pos") to Sequence(
                                                        RuleWithVariables(OneOrMore(Digit())),
                                                        Action<Any> {
                                                            position.set(pop().toString().toIntOrNull() ?: 0)
                                                        }
                                                ),
                                                "state" to Sequence(
                                                        RuleWithVariables(OneOrMore(Digit())),
                                                        Action<Any> { state.set(pop().toString().toIntOrNull() ?: 0) }
                                                ),
                                                "transition" to Sequence(
                                                        RuleWithVariables(OneOrMore(Digit())),
                                                        Action<Any> {
                                                            transition.set(pop().toString().toIntOrNull() ?: 0)
                                                        }
                                                )
                                        ),

                                        arrayOf(
                                                this::NoopFail,
                                                this::NoopFail,
                                                contextFunc { position.set(0) },
                                                contextFunc { state.set(1) },
                                                contextFunc { transition.set(2) }
                                        )
                                ),

                                Action<Any> {
                                    pushTmp(cmd, character.get())
                                    pushTmp(cmd, sprite.get())
                                    pushTmp(cmd, position.get())

                                    pushTmp(cmd, state.get())
                                    pushTmp(cmd, transition.get())

                                    return@Action true
                                }
                        )
                ),

                pushStackWithHead(cmd)
        )
    }

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinEntry {
        val characterStr = rawParams[0].toString()
        val character = parser.customIdentifiers[characterStr]
                ?: (parser.hopesPeakGame ?: UnknownHopesPeakGame).characterIdentifiers[characterStr]
                ?: characterStr.toIntOrNull() ?: 0
        val sprite = rawParams[1].toString().toIntOrNull() ?: 0
        val position = rawParams[2].toString().toIntOrNull() ?: 0

        val state = rawParams[3].toString().toIntOrNull() ?: 0
        val transition = rawParams[4].toString().toIntOrNull() ?: 0

        return when (parser.hopesPeakGame) {
            DR1 -> SpriteEntry(position, character, sprite, state, transition)
            DR2 -> SpriteEntry(position, character, sprite, state, transition)
            else -> TODO("Bust Sprites haven't been documented for ${parser.hopesPeakGame}")
        }
    }
}
