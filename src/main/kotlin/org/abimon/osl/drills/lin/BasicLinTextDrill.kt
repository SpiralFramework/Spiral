package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.UDG
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.abimon.spiral.core.objects.scripting.lin.udg.UDGTextEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicLinTextDrill : DrillHead<LinScript> {
    val cmd = "BASIC-LIN-TEXT"

    override val klass: KClass<LinScript> = LinScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            FirstOf(
                                    Sequence(
                                            "0x",
                                            Optional("0"),
                                            "2"
                                    ),
                                    IgnoreCase("Text")
                            ),
                            '|',
                            pushDrillHead(cmd, this@BasicLinTextDrill),
                            LinText(cmd),
                            pushTmpAction(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        return when(parser.game) {
            DR1 -> TextEntry("${rawParams[0]}", -1)
            DR2 -> TextEntry("${rawParams[0]}", -1)
            UDG -> UDGTextEntry("${rawParams[0]}", -1)
            else -> TODO("Text is not documented for ${parser.game}")
        }
    }
}