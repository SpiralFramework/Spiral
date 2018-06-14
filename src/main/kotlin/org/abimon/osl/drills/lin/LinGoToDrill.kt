package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.GoToLabelEntry
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinGoToDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class
    val cmd = "LIN-GOTO"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            FirstOf("Goto", "Go To"),
                            pushDrillHead(cmd, this@LinGoToDrill),
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
            DR1 -> GoToLabelEntry((first shl 8) or second)
            DR2 -> GoToLabelEntry((first shl 8) or second)
            else -> TODO("Label Goto's are not documented in ${parser.game}")
        }
    }
}