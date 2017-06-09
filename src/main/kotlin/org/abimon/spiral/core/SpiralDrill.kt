package org.abimon.spiral.core

import org.abimon.spiral.core.drills.BasicSpiralDrill
import org.abimon.spiral.core.drills.NamedSpiralDrill
import org.abimon.util.ParamList
import org.abimon.util.operateOnTmpStack
import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner

@BuildParseTree
open class SpiralDrill: BaseParser<Any>() {
    companion object {
        val parser: SpiralDrill = Parboiled.createParser(SpiralDrill::class.java)
        val runner = ReportingParseRunner<Any>(parser.SpiralFile())
    }

    open fun SpiralFile(): Rule = Sequence(ParamList("DRILL", SpiralLine()), operateOnTmpStack(this, "DRILL") { push(it) })

    open fun SpiralLine(): Rule = FirstOf(BasicSpiralDrill.Syntax(this), NamedSpiralDrill.Syntax(this))
}