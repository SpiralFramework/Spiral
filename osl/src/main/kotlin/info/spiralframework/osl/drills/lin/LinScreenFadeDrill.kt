package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.formats.scripting.lin.ScreenFadeEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinScreenFadeDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class
    val cmd = "LIN-SCREEN-FADE"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Fade",
                            pushDrillHead(cmd, this@LinScreenFadeDrill),
                            InlineWhitespace(),
                            FirstOf(
                                    Sequence(
                                            "in",
                                            pushTmpAction(cmd, 0),
                                            InlineWhitespace(),
                                            "from",
                                            InlineWhitespace()
                                    ),
                                    Sequence(
                                            "out",
                                            pushTmpAction(cmd, 1),
                                            InlineWhitespace(),
                                            "to",
                                            InlineWhitespace()
                                    )
                            ),
                            FirstOf(
                                    Sequence(
                                            "black",
                                            pushTmpAction(cmd, 1)
                                    ),
                                    Sequence(
                                            "white",
                                            pushTmpAction(cmd, 2)
                                    ),
                                    Sequence(
                                            "red",
                                            pushTmpAction(cmd, 3)
                                    )
                            ),
                            InlineWhitespace(),
                            "for",
                            InlineWhitespace(),
                            FrameCount(),
                            pushTmpAction(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val fadeIn = rawParams[0].toString().toIntOrNull() == 0
        val colour = rawParams[1].toString().toIntOrNull() ?: 1
        val frameCount = rawParams[2].toString().toIntOrNull() ?: 60

        return when(parser.hopesPeakGame) {
            DR1 -> ScreenFadeEntry(fadeIn, colour, frameCount)
            DR2 -> ScreenFadeEntry(fadeIn, colour, frameCount)
            else -> TODO("Screen Fades are not documented for ${parser.hopesPeakGame}")
        }
    }
}
