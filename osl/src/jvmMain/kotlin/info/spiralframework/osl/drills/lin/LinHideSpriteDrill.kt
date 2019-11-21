package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.SpriteEntry
import org.parboiled.Action
import org.parboiled.Rule
import java.util.*
import kotlin.reflect.KClass

object LinHideSpriteDrill : DrillHead<LinEntry> {
    val cmd: String = "LIN-HIDE-SPRITE"
    val NAME = info.spiralframework.osl.AllButMatcher(charArrayOf(':', '\n'))
    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinEntry> = LinEntry::class

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
                                        return@Action name in customIdentifiers || name in (hopesPeakGame
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinEntry {
        val characterStr = rawParams[0].toString()
        val character = parser.customIdentifiers[characterStr]
                ?: (parser.hopesPeakGame ?: UnknownHopesPeakGame).characterIdentifiers[characterStr]
                ?: characterStr.toIntOrNull() ?: 0

        return when(parser.hopesPeakGame) {
            DR1 -> SpriteEntry(0, character, 0, 4, 2)
            DR2 -> SpriteEntry(0, character, 0, 4, 2)
            else -> TODO("Sprites are not documented for ${parser.hopesPeakGame}")
        }
    }
}
