package org.abimon.osl.drills

import org.abimon.osl.*
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Action
import org.parboiled.Rule

//TODO: Support DR2 op codes too
object NamedSpiralDrill : DrillHead {
    val cmd = "NAMED"

    override fun Syntax(parser: OpenSpiralLanguageParser): Rule = parser.makeCommand {
        Sequence(
                clearTmpStack(cmd),
                OneOrMore(LineCodeMatcher),
                Action<Any> {
                    val name = match()
                    parser.game.opCodes.values.any { (names) -> name in names }
                },
                pushTmpAction(this, cmd, this@NamedSpiralDrill),
                pushTmpAction(this, cmd),
                Optional(
                        '|'
                ),
                Optional(
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


    override fun formScripts(rawParams: Array<Any>, game: HopesPeakDRGame): Array<LinScript> {
        //rawParams[0] = SpiralData.dr1OpCodes.entries.first { (_, pair) -> pair.second.equals("${rawParams[0]}", true) }.key.toString(16)
        return BasicSpiralDrill.formScripts(rawParams, game)
    }
}