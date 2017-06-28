package org.abimon.spiral.core.drills

import org.abimon.spiral.core.lin.LinScript
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.util.*
import org.parboiled.BaseParser
import org.parboiled.Rule

object BasicTextDrill : DrillHead {
    val cmd = "BASIC_TEXT"

    override fun Syntax(parser: BaseParser<Any>): Rule = parser.makeCommand {
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
                pushTmpAction(this, cmd, this@BasicTextDrill),
                OneOrMore(LineCodeMatcher),
                pushTmpAction(this, cmd),
                pushTmpStack(this, cmd)
        )
    }

    override fun formScripts(rawParams: Array<Any>): Array<LinScript> = arrayOf(TextEntry("${rawParams[0]}"))
}