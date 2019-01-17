package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.wrd.TextEntry
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicWrdTextDrill : DrillHead<WrdScript> {
    val cmd = "BASIC-WRD-TEXT"

    override val klass: KClass<WrdScript> = WrdScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Text",
                            '|',
                            pushDrillHead(cmd, this@BasicWrdTextDrill),
                            WrdText(cmd),
                            ensureParam(cmd, 1, EnumWordScriptCommand.STRING),
                            pushTmpAction(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript {
        val text = rawParams[0].toString()
        val index = parser.wordScriptStrings.indexOf(text)
        if (index == -1)
            error("$text is not in our set of strings, something has gone wrong")
        return TextEntry(index)
    }
}