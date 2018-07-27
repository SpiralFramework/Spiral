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
    val NAME = AllButMatcher(charArrayOf(':', '\n'))
    val NUMERAL_REGEX = "\\d+".toRegex()

    override val klass: KClass<LinScript> = LinScript::class

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Focus camera on",
                            pushDrillHead(cmd, this@LinCameraFocusDrill),
                            InlineWhitespace(),
                            FirstOf(
                                    Parameter(cmd),
                                    Sequence(
                                            OneOrMore(Digit()),
                                            pushTmpAction(cmd)
                                    )
                            ),
                            pushTmpAction(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val uiStateStr = rawParams[0].toString()

        val uiState: Int

        uiState = uiStateStr.toIntOrNull() ?: 0

        return when(parser.hopesPeakGame) {
            DR1 -> ChangeUIEntry(26, uiState)
            else -> TODO("Camera focus is not documented for ${parser.hopesPeakGame}")
        }
    }
}