package org.abimon.osl.drills

import org.abimon.osl.LineCodeMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicTextDrill : DrillHead<LinScript> {
    val cmd = "BASIC_TEXT"

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
                    pushTmpAction(cmd, this@BasicTextDrill),
                    OneOrMore(LineCodeMatcher),
                    pushTmpAction(cmd),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript = TextEntry("${rawParams[0]}", -1)
}