package org.abimon.osl.drills

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Rule

interface DrillHead {
    fun Syntax(parser: OpenSpiralLanguageParser): Rule
    fun formScripts(rawParams: Array<Any>, game: HopesPeakDRGame): Array<LinScript>

    fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {}
}