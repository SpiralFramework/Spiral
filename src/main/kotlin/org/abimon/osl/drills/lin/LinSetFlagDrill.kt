package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.SetFlagEntry
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

        return when(parser.game) {
            DR1 -> SetFlagEntry(group, flagID, value)
            DR2 -> SetFlagEntry(group, flagID, value)
            else -> TODO("Flag Sets are not documented for ${parser.game}")
        }
    }
}