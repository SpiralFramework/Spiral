package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.SpeakerEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinSpeakerDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class
    val cmd = "LIN-SPEAKER"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Speaker|",
                            pushDrillHead(cmd, this@LinSpeakerDrill),
                            OptionalInlineWhitespace(),
                            SpeakerName(),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val first = rawParams[0].toString().toIntOrNull() ?: 0

        return when (parser.game) {
            DR1 -> SpeakerEntry(first)
            DR2 -> SpeakerEntry(first)
            else -> TODO("Label Goto's are not documented in ${parser.game}")
        }
    }
}