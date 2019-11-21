package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.dr1.DR1TrialCameraEntry
import info.spiralframework.formats.scripting.lin.dr2.DR2TrialCameraEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinTrialCameraDrill: DrillHead<LinEntry> {
    override val klass: KClass<LinEntry> = LinEntry::class
    val cmd = "LIN-TRIAL-CAMERA"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Trial Camera",
                            FirstOf('|', ':'),
                            pushDrillHead(cmd, this@LinTrialCameraDrill),
                            OptionalInlineWhitespace(),
                            SpeakerName(),
                            pushTmpFromStack(cmd),
                            CommaSeparator(),
                            TrialCameraID(),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinEntry {
        val first = rawParams[0].toString().toIntOrNull() ?: 0
        val second = rawParams[1].toString().toIntOrNull() ?: 0
        val third = rawParams[2].toString().toIntOrNull() ?: 0

        val motionID = (second shl 8) or third

        return when (parser.hopesPeakGame) {
            DR1 -> DR1TrialCameraEntry(first, motionID)
            DR2 -> DR2TrialCameraEntry(first, motionID, 0, 0, 0)
            else -> TODO("Trial Camera's are not documented in ${parser.hopesPeakGame}")
        }
    }
}
