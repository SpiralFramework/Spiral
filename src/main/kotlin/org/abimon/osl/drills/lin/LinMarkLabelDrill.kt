package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.SetLabelEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinMarkLabelDrill: DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class

    val cmd = "LIN-MARK-LABEL"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("Set Label", "Mark Label"),
                    pushTmpAction(cmd, this@LinMarkLabelDrill),
                    OptionalWhitespace(),
                    Label(),
                    pushTmpFromStack(cmd),
                    pushTmpFromStack(cmd),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val first = rawParams[0].toString().toIntOrNull() ?: 0
        val second = rawParams[1].toString().toIntOrNull() ?: 0

        return SetLabelEntry((first shl 8) or second)
    }
}