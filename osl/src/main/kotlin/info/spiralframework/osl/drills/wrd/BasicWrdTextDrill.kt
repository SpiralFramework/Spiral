package info.spiralframework.osl.drills.wrd

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.scripting.EnumWordScriptCommand
import info.spiralframework.formats.scripting.wrd.TextEntry
import info.spiralframework.formats.scripting.wrd.WrdScript
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
