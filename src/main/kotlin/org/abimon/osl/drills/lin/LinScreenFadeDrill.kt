package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.ScreenFadeEntry
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

        return when(parser.gameContext) {
            DR1 -> ScreenFadeEntry(fadeIn, colour, frameCount)
            DR2 -> ScreenFadeEntry(fadeIn, colour, frameCount)
            else -> TODO("Screen Fades are not documented for ${parser.gameContext}")
        }
    }
}