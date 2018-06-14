package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.SetLabelEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinMarkLabelDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class

    val cmd = "LIN-MARK-LABEL"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            FirstOf("Set Label", "Mark Label"),
                            pushDrillHead(cmd, this@LinMarkLabelDrill),
                            InlineWhitespace(),
                            Label(),
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val first = rawParams[0].toString().toIntOrNull() ?: 0
        val second = rawParams[1].toString().toIntOrNull() ?: 0

        return when(parser.game) {
            DR1 -> SetLabelEntry((first shl 8) or second)
            DR2 -> SetLabelEntry((first shl 8) or second)

            else -> TODO("Label Marking is not documented for ${parser.game}")
        }
    }
}