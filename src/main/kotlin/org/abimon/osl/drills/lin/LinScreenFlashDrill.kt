package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
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
                            Whitespace(),
                            Optional("the screen", Whitespace()),
                            Colour(),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd),
                            Whitespace(),
                            "over",
                            Whitespace(),
                            FrameCount(),
                            pushTmpFromStack(cmd),
                            ",",
                            OptionalWhitespace(),
                            "hold for",
                            Whitespace(),
                            FrameCount(),
                            pushTmpFromStack(cmd),
                            ',',
                            OptionalWhitespace(),
                            Optional("and", Whitespace()),
                            "fade out over",
                            Whitespace(),
                            FrameCount(),
                            pushTmpFromStack(cmd),
                            FirstOf(
                                    Sequence(
                                            ',',
                                            OptionalWhitespace(),
                                            "with opacity",
                                            Whitespace(),
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

        return ScreenFlashEntry(r, g, b, fadeInOver, holdFor, fadeOutOver, opacity)
    }
}