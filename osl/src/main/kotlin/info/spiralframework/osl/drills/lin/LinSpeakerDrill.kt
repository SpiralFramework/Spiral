package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.formats.scripting.lin.SpeakerEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinSpeakerDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class
    val cmd = "LIN-SPEAKER"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Speaker",
                            FirstOf('|', ':'),
                            pushDrillHead(cmd, this@LinSpeakerDrill),
                            OptionalInlineWhitespace(),
                            SpeakerName(),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val first = rawParams[0].toString().toIntOrNull() ?: 0

        return when (parser.hopesPeakGame) {
            DR1 -> SpeakerEntry(first)
            DR2 -> SpeakerEntry(first)
            else -> TODO("Label Goto's are not documented in ${parser.hopesPeakGame}")
        }
    }
}
