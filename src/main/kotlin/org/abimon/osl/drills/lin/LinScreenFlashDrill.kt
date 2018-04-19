package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinScreenFlashDrill: DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "Flash",
                    Whitespace(),
                    Optional("the screen", Whitespace()),
                    Colour(),
                    "over",
                    Whitespace(),
                    FrameCount(),
                    ",",
                    Whitespace(),
                    "hold for",
                    Whitespace()
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}