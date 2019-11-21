package info.spiralframework.osl.drills.lin

import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.game.hpa.UDG
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.SpeakerEntry
import info.spiralframework.formats.scripting.lin.UnknownEntry
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinSpeakerDrill : DrillHead<LinEntry> {
    override val klass: KClass<LinEntry> = LinEntry::class
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinEntry {
        val first = rawParams[0].toString().toIntOrNull() ?: 0

        return when (parser.hopesPeakGame) {
            DR1 -> SpeakerEntry(first)
            DR2 -> SpeakerEntry(first)
            UDG -> UnknownEntry(0x15, intArrayOf(first))
            else -> TODO("Label Goto's are not documented in ${parser.hopesPeakGame}")
        }
    }
}
