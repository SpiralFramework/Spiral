package org.abimon.osl.drills.lin

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicLinTextDrill : DrillHead<LinScript> {
    val cmd = "BASIC-LIN-TEXT"

    override val klass: KClass<LinScript> = LinScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf(
                            Sequence(
                                    "0x",
                                    Optional("0"),
                                    "2"
                            ),
                            IgnoreCase("Text")
                    ),
                    '|',
                    pushTmpAction(cmd, this@BasicLinTextDrill),
                    LinText(cmd),
                    pushTmpAction(cmd),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript = TextEntry("${rawParams[0]}", -1)
}