package org.abimon.osl.drills.lin

import org.abimon.osl.AllButMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.ChangeUIEntry
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinCameraFocusDrill : DrillHead<LinScript> {
    val cmd: String = "LIN-CAMERA-FOCUS"

    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinScript> = LinScript::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            FirstOf("Focus camera on", "Set camera focus to", "Camera Focus:"),
                            pushDrillHead(cmd, this@LinCameraFocusDrill),
                            InlineWhitespace(),
                            OneOrMore(Digit()),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val uiState = rawParams[0].toString().toIntOrNull() ?: 0

        return when(parser.hopesPeakGame) {
            DR1 -> ChangeUIEntry(26, uiState)
            else -> TODO("Camera focus is not documented for ${parser.hopesPeakGame}")
        }
    }
}