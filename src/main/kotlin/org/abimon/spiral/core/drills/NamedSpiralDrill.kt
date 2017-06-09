package org.abimon.spiral.core.drills

import org.abimon.spiral.core.SpiralData
import org.abimon.spiral.core.lin.LinScript
import org.abimon.util.*
import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Rule

object NamedSpiralDrill : DrillHead {
    val cmd = "NAMED"

    override fun Syntax(parser: BaseParser<Any>): Rule = parser.makeCommand {
        Sequence(
                clearTmpStack(cmd),
                OneOrMore(LineCodeMatcher),
                Action<Any> { SpiralData.opCodes.values.any { (_, name) -> name.equals(match(), true) } },
                pushTmpAction(this, cmd, this@NamedSpiralDrill),
                pushTmpAction(this, cmd),
                Optional(
                        '|',
                        ParamList(
                                cmd,
                                Sequence(
                                        OneOrMore(Digit()),
                                        pushToStack(this)
                                ),
                                Sequence(
                                        ',',
                                        Optional(Whitespace())
                                )
                        )
                ),
                pushTmpStack(this, cmd)
        )
    }

    override fun formScript(rawParams: Array<Any>): LinScript {
        rawParams[0] = SpiralData.opCodes.entries.first { (_, pair) -> pair.second.equals("${rawParams[0]}", true) }.key.toString(16)
        return BasicSpiralDrill.formScript(rawParams)
    }
}