package org.abimon.osl

import org.abimon.osl.drills.BasicSpiralDrill
import org.abimon.osl.drills.BasicTextDrill
import org.abimon.osl.drills.DialogueDrill
import org.abimon.osl.drills.NamedSpiralDrill
import org.abimon.osl.drills.circuits.ChangeGameDrill
import org.abimon.osl.drills.circuits.EchoDrill
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.parboiled.Action
import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree

@BuildParseTree
open class OpenSpiralLanguageParser : BaseParser<Any>() {
    companion object {
        val parser: OpenSpiralLanguageParser = Parboiled.createParser(OpenSpiralLanguageParser::class.java)
    }

    var game: HopesPeakDRGame = UnknownHopesPeakGame

    open fun OpenSpiralLanguage(): Rule = Sequence(
            clearState(),
            Sequence("OSL Script\n", ParamList("DRILL", SpiralTextLine())),
            EOI,
            Action<Any> {
                game = UnknownHopesPeakGame
                tmpStack["DRILL"]?.forEach(this::pushValue)
                return@Action true
            }
    )

    open fun SpiralTextLine(): Rule = FirstOf(
            BasicTextDrill.Syntax(this),
            DialogueDrill.Syntax(this),
            BasicSpiralDrill.Syntax(this),
            NamedSpiralDrill.Syntax(this),

            ChangeGameDrill.Syntax(this),
            EchoDrill.Syntax(this),

//            Comment(),
//            Whitespace()
            EMPTY
    )

//    override fun toRule(obj: Any?): Rule {
//        when (obj) {
//            is DrillHead -> return obj.Syntax(this)
//            else -> return super.toRule(obj)
//        }
//    }

    fun pushValue(value: Any): Unit {
        this.push(value)
    }
}