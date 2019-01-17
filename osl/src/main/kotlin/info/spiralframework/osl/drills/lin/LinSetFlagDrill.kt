package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.formats.scripting.lin.SetFlagEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinSetFlagDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class

    val cmd = "LIN-SET-FLAG"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Set Flag",
                            OptionalInlineWhitespace(),
                            pushDrillHead(cmd, this@LinSetFlagDrill),
                            Flag(),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd),
                            InlineWhitespace(),
                            "to",
                            InlineWhitespace(),
                            FlagValue(),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val group = rawParams[0].toString().toIntOrNull() ?: 0
        val flagID = rawParams[1].toString().toIntOrNull() ?: 0

        val value = rawParams[2].toString().toIntOrNull() ?: 0

        return when(parser.hopesPeakGame) {
            DR1 -> SetFlagEntry(group, flagID, value)
            DR2 -> SetFlagEntry(group, flagID, value)
            else -> TODO("Flag Sets are not documented for ${parser.hopesPeakGame}")
        }
    }
}
