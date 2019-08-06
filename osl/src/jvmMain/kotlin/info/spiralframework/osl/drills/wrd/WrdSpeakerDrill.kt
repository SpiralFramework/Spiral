package info.spiralframework.osl.drills.wrd

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.scripting.EnumWordScriptCommand
import info.spiralframework.formats.scripting.wrd.UnknownEntry
import info.spiralframework.formats.scripting.wrd.WrdScript
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object WrdSpeakerDrill : DrillHead<WrdScript> {
    val cmd = "BASIC-WRD-SPEAKER"

    override val klass: KClass<WrdScript> = WrdScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Speaker",
                            '|',
                            pushDrillHead(cmd, this@WrdSpeakerDrill),
                            ParameterToStack(),
                            Action<Any> {
                                val name = pop().toString()

                                val id = V3.characterIdentifiers[name] ?: name.toIntOrNull() ?: 0
                                val parameter = V3.characterIDs[id] ?: "C000_Saiha"

                                pushTmp(cmd, parameter)

                                return@Action true
                            },
                            ensureParam(cmd, 1, EnumWordScriptCommand.PARAMETER),
                            pushTmpAction(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript {
        val text = rawParams[0].toString()
        val index = parser.wordScriptParameters.indexOf(text)
        if (index == -1)
            error("$text is not in our set of parameters, something has gone wrong")
        return UnknownEntry(0x1D, intArrayOf(index))
    }
}
