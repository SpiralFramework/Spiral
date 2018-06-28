package org.abimon.osl.drills.lin

import org.abimon.osl.AllButMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.SpriteEntry
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinBustSpriteDrill : DrillHead<LinScript> {
    val cmd: String = "LIN-BUST-SPRITE"
    val NAME = AllButMatcher(charArrayOf(':', '\n'))
    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinScript> = LinScript::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    FirstOf(
                            Sequence(
                                    "Display sprite for ",
                                    pushDrillHead(cmd, this@LinBustSpriteDrill),
                                    FirstOf(
                                            Parameter(cmd),
                                            Sequence(
                                                    OneOrMore(Digit()),
                                                    pushTmpAction(cmd)
                                            )
                                    ),
                                    Action<Any> {
                                        val name = peekTmpAction(cmd)?.toString() ?: ""
                                        return@Action name in customIdentifiers || name in (hopesPeakGame
                                                ?: UnknownHopesPeakGame).characterIdentifiers || name.matches(NUMERAL_REGEX)
                                    },
                                    " with ID ",
                                    OneOrMore(Digit()),
                                    pushTmpAction(cmd)
                            ),
                            Sequence(
                                    "Display",
                                    pushDrillHead(cmd, this@LinBustSpriteDrill),
                                    InlineWhitespace(),
                                    FirstOf(
                                            Parameter(cmd),
                                            Sequence(
                                                    OneOrMore(Digit()),
                                                    pushTmpAction(cmd)
                                            )
                                    ),
                                    Action<Any> {
                                        val name = peekTmpAction(cmd)?.toString() ?: ""
                                        return@Action name in customIdentifiers || name in (hopesPeakGame
                                                ?: UnknownHopesPeakGame).characterIdentifiers || name.matches(NUMERAL_REGEX)
                                    },
                                    InlineWhitespace(),
                                    "pose",
                                    InlineWhitespace(),
                                    OneOrMore(Digit()),
                                    pushTmpAction(cmd)
                            )
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val characterStr = rawParams[0].toString()
        val character = parser.customIdentifiers[characterStr]
                ?: (parser.hopesPeakGame ?: UnknownHopesPeakGame).characterIdentifiers[characterStr]
                ?: characterStr.toIntOrNull() ?: 0
        val sprite = rawParams[1].toString().toIntOrNull() ?: 0

        return when(parser.hopesPeakGame) {
            DR1 -> SpriteEntry(0, character, sprite, 1, 2)
            DR2 -> SpriteEntry(0, character, sprite, 1, 2)
            else -> TODO("Bust Sprites haven't been documented for ${parser.hopesPeakGame}")
        }
    }
}