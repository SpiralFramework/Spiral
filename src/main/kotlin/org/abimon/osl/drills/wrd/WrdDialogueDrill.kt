package org.abimon.osl.drills.wrd

import org.abimon.osl.AllButMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.wrd.TextEntry
import org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object WrdDialogueDrill : DrillHead<Array<WrdScript>> {
    val NAME = AllButMatcher(charArrayOf(':', '\n'))
    val cmd = "WRD-DIALOGUE"

    override val klass: KClass<Array<WrdScript>> = Array<WrdScript>::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            OneOrMore(NAME),
                            Action<Any> {
                                match() in customIdentifiers || match() in V3.characterIdentifiers
                            },
                            pushDrillHead(cmd, this@WrdDialogueDrill),
                            Action<Any> {
                              val name = match()

                                val id = V3.characterIdentifiers[name] ?: name.toIntOrNull() ?: 0
                                val parameter = V3.characterIDs[id] ?: "C000_Saiha"

                                pushTmp(cmd, parameter)

                                return@Action true
                            },
                            ensureParam(cmd, 1, EnumWordScriptCommand.PARAMETER),
                            ':',
                            OptionalInlineWhitespace(),
                            WrdText(cmd),
                            ensureParam(cmd, 2, EnumWordScriptCommand.STRING)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<WrdScript> {
        val speakerName = rawParams[0].toString()
        val speakerIndex = parser.wordScriptParameters.indexOf(speakerName)
        if (speakerIndex == -1)
            error("$speakerName is not in our set of parameters, something has gone wrong")

        val text = rawParams[1].toString()
        val textIndex = parser.wordScriptStrings.indexOf(text)
        if (textIndex == -1)
            error("$text is not in our set of strings, something has gone wrong")

        return arrayOf(
                UnknownEntry(0x1D, intArrayOf(speakerIndex)),
                TextEntry(textIndex),
                UnknownEntry(0x47, intArrayOf())
        )
    }
}