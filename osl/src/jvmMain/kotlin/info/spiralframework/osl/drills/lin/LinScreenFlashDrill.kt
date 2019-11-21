package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.ScreenFlashEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinScreenFlashDrill : DrillHead<LinEntry> {
    override val klass: KClass<LinEntry> = LinEntry::class
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinEntry {
        val r = rawParams[0].toString().toIntOrNull() ?: 0
        val g = rawParams[1].toString().toIntOrNull() ?: 0
        val b = rawParams[2].toString().toIntOrNull() ?: 0

        val fadeInOver = rawParams[3].toString().toIntOrNull() ?: 0
        val holdFor = rawParams[4].toString().toIntOrNull() ?: 0
        val fadeOutOver = rawParams[5].toString().toIntOrNull() ?: 0
        val opacity = rawParams[6].toString().toIntOrNull() ?: 0

        return when(parser.hopesPeakGame) {
            DR1 -> ScreenFlashEntry(r, g, b, fadeInOver, holdFor, fadeOutOver, opacity)
            DR2 -> ScreenFlashEntry(r, g, b, fadeInOver, holdFor, fadeOutOver, opacity)
            else -> TODO("Screen Flashes are not documented for ${parser.hopesPeakGame}")
        }
    }
}
