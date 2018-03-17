package org.abimon.spiral.core

import org.abimon.spiral.core.drills.BasicSpiralDrill
import org.abimon.spiral.core.drills.BasicTextDrill
import org.abimon.spiral.core.drills.DialogueDrill
import org.abimon.spiral.core.drills.NamedSpiralDrill
import org.abimon.spiral.util.LineMatcher
import org.abimon.spiral.util.ParamList
import org.abimon.spiral.util.clearState
import org.abimon.spiral.util.operateOnTmpStack
import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner

@BuildParseTree
open class SpiralDrill : BaseParser<Any>() {
    companion object {
        val parser: SpiralDrill = Parboiled.createParser(SpiralDrill::class.java)
        val stxtRunner: ReportingParseRunner<Any> = ReportingParseRunner(parser.SpiralTextFile())
    }

    /** Spiral Text */
    open fun SpiralTextFile(): Rule = Sequence(
            clearState(),
            FirstOf(
                    Sequence("OSL Script", ParamList("DRILL", SpiralTextLine())),
                    ParamList("DRILL", SpiralOpCodeLine())
            ),
            operateOnTmpStack(this, "DRILL") { push(it) }
    )

    open fun SpiralOpCodeLine(): Rule = FirstOf(
            BasicTextDrill.Syntax(this),
            BasicSpiralDrill.Syntax(this),
            NamedSpiralDrill.Syntax(this),
            Comment(),
            EOI
    )

    open fun SpiralTextLine(): Rule = FirstOf(
            BasicTextDrill.Syntax(this),
            DialogueDrill.Syntax(this),
            BasicSpiralDrill.Syntax(this),
            NamedSpiralDrill.Syntax(this),
            Comment(),
            EOI
    )

    open fun Comment(): Rule = FirstOf(
            Sequence("//", ZeroOrMore(LineMatcher)),
            Sequence("#", ZeroOrMore(LineMatcher))
    )
}