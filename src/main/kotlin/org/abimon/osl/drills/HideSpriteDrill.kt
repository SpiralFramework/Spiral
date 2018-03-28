package org.abimon.osl.drills

import org.abimon.osl.AllButMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.SpriteEntry
import org.parboiled.Action
import org.parboiled.Rule
import java.util.*
import kotlin.reflect.KClass

object HideSpriteDrill : DrillHead<LinScript> {
    val cmd: String = "HIDE-SPRITE"
    val NAME = AllButMatcher(charArrayOf(':', '\n'))
    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinScript> = LinScript::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            FirstOf(
                    Sequence(
                            clearTmpStack(cmd),
                            "Hide" ,
                            Whitespace(),
                            Optional("sprite for "),
                            pushTmpAction(cmd, this@HideSpriteDrill),
                            FirstOf(
                                    Parameter(cmd),
                                    Sequence(
                                            OneOrMore(Digit()),
                                            pushTmpAction(cmd)
                                    )
                            ),
                            Action<Any> {
                                val name = peekTmpAction(cmd)?.toString() ?: ""
                                return@Action name in customIdentifiers || name in game.characterIdentifiers || name.matches(NUMERAL_REGEX)
                            },
                            pushTmpStack(cmd)
                    ),
                    Sequence(
                            UUID.randomUUID().toString(),
                            "s"
                    )
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val characterStr = rawParams[0].toString()
        val character = parser.customIdentifiers[characterStr] ?: parser.game.characterIdentifiers[characterStr] ?: characterStr.toIntOrNull() ?: 0
        return SpriteEntry(0, character, 0, 4, 2)
    }
}