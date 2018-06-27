package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.ScreenFlashEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinScreenFlashDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class
    val cmd = "LIN-SCREEN-FLASH"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Flash",
                            pushDrillHead(cmd, this@LinScreenFlashDrill),
                            InlineWhitespace(),
                            Optional("the screen", InlineWhitespace()),
                            Colour(),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd),
                            InlineWhitespace(),
                            "over",
                            InlineWhitespace(),
                            FrameCount(),
                            pushTmpFromStack(cmd),
                            ",",
                            OptionalInlineWhitespace(),
                            "hold for",
                            InlineWhitespace(),
                            FrameCount(),
                            pushTmpFromStack(cmd),
                            ',',
                            OptionalInlineWhitespace(),
                            Optional("and", InlineWhitespace()),
                            "fade out over",
                            InlineWhitespace(),
                            FrameCount(),
                            pushTmpFromStack(cmd),
                            FirstOf(
                                    Sequence(
                                            ',',
                                            OptionalInlineWhitespace(),
                                            "with opacity",
                                            InlineWhitespace(),
                                            OneOrMore(Digit()),
                                            pushTmpAction(cmd)
                                    ),
                                    pushTmpAction(cmd, 255)
                            )
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val r = rawParams[0].toString().toIntOrNull() ?: 0
        val g = rawParams[1].toString().toIntOrNull() ?: 0
        val b = rawParams[2].toString().toIntOrNull() ?: 0

        val fadeInOver = rawParams[3].toString().toIntOrNull() ?: 0
        val holdFor = rawParams[4].toString().toIntOrNull() ?: 0
        val fadeOutOver = rawParams[5].toString().toIntOrNull() ?: 0
        val opacity = rawParams[6].toString().toIntOrNull() ?: 0

        return when(parser.gameContext) {
            DR1 -> ScreenFlashEntry(r, g, b, fadeInOver, holdFor, fadeOutOver, opacity)
            DR2 -> ScreenFlashEntry(r, g, b, fadeInOver, holdFor, fadeOutOver, opacity)
            else -> TODO("Screen Flashes are not documented for ${parser.gameContext}")
        }
    }
}