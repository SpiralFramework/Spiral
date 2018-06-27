package org.abimon.osl.drills.lin

import org.abimon.osl.AllButMatcher
import org.abimon.osl.GameContext
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.SpriteEntry
import org.parboiled.Action
import org.parboiled.Rule
import java.util.*
import kotlin.reflect.KClass

object LinHideSpriteDrill : DrillHead<LinScript> {
    val cmd: String = "LIN-HIDE-SPRITE"
    val NAME = AllButMatcher(charArrayOf(':', '\n'))
    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinScript> = LinScript::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    FirstOf(
                            Sequence(
                                    "Hide",
                                    InlineWhitespace(),
                                    Optional("sprite for "),
                                    pushDrillHead(cmd, this@LinHideSpriteDrill),
                                    FirstOf(
                                            Parameter(cmd),
                                            Sequence(
                                                    OneOrMore(Digit()),
                                                    pushTmpAction(cmd)
                                            )
                                    ),
                                    Action<Any> {
                                        val name = peekTmpAction(cmd)?.toString() ?: ""
                                        return@Action name in customIdentifiers || name in ((gameContext as? GameContext.HopesPeakGameContext)?.game
                                                ?: UnknownHopesPeakGame).characterIdentifiers || name.matches(NUMERAL_REGEX)
                                    }
                            ),
                            Sequence(
                                    UUID.randomUUID().toString(),
                                    "s"
                            )
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val characterStr = rawParams[0].toString()
        val character = parser.customIdentifiers[characterStr] ?: (parser.gameContext as? HopesPeakDRGame
                ?: UnknownHopesPeakGame).characterIdentifiers[characterStr] ?: characterStr.toIntOrNull() ?: 0
        return when((parser.gameContext as? GameContext.HopesPeakGameContext)?.game ?: UnknownHopesPeakGame) {
            DR1 -> SpriteEntry(0, character, 0, 4, 2)
            DR2 -> SpriteEntry(0, character, 0, 4, 2)
            else -> TODO("Sprites are not documented for ${parser.gameContext}")
        }
    }
}