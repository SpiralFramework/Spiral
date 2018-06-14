package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.wrd.UnknownEntry
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.core.utils.and
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicWrdSpiralDrill : DrillHead<WrdScript> {
    val cmd = "BASIC-WRD"

    override val klass: KClass<WrdScript> = WrdScript::class

    @Suppress("UNCHECKED_CAST")
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "0x",
                    OneOrMore(Digit(16)),
                    pushDrillHead(cmd, this@BasicWrdSpiralDrill),
                    pushTmpAction(cmd),
                    Optional(
                            '|'
                    ),
                    OptionalInlineWhitespace(),
                    Optional(
                            ParamList(
                                    cmd,
                                    ParameterToStack(),
                                    Sequence(
                                            ',',
                                            OptionalInlineWhitespace()
                                    )
                            )
                    ),
                    operateOnTmpActions(cmd) { stack ->
                        val wrdParams = stack.drop(2).map(Any::toString)

//                        val commandEnum = V3.opCodeCommandEntries[opCode]
//                        val commands = data["wrd-command-$commandEnum"] as? MutableList<String> ?: ArrayList<String>()

                        for (param in wrdParams) {
                            if (param.startsWith("LABEL:")) {
                                val label = param.substringAfter("LABEL:")
                                ensureString(label, EnumWordScriptCommand.LABEL)
                            } else if (param.startsWith("PARAMETER:")) {
                                val parameter = param.substringAfter("PARAMETER:")
                                ensureString(parameter, EnumWordScriptCommand.PARAMETER)
                            } else if (param.startsWith("STRING:")) {
                                val string = param.substringAfter("STRING:")
                                ensureString(string, EnumWordScriptCommand.STRING)
                            }
                        }
                    },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript = formScript(parser, rawParams)

    @Suppress("UNCHECKED_CAST")
    fun formScript(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript {
        val opCode = "${rawParams[0]}".toInt(16)
        val wrdParams = rawParams.copyOfRange(1, rawParams.size).map(Any::toString)
        val params = ArrayList<Int>()

        for (param in wrdParams) {
            if (param.startsWith("LABEL:")) {
                val label = param.substringAfter("LABEL:")
                val index = parser.wordScriptLabels.indexOf(label)
                if (index == -1)
                    error("$label is not in our set of labels, something has gone wrong")
                params.add(index)
            } else if (param.startsWith("PARAMETER:")) {
                val parameter = param.substringAfter("PARAMETER:")
                val index = parser.wordScriptParameters.indexOf(parameter)
                if (index == -1)
                    error("$parameter is not in our set of parameters, something has gone wrong")
                params.add(index)
            } else if (param.startsWith("STRING:")) {
                val string = param.substringAfter("STRING:")
                val index = parser.wordScriptStrings.indexOf(string)
                if (index == -1)
                    error("$string is not in our set of strings, something has gone wrong")
                params.add(index)
            } else
                params.add(param.toIntOrNull() ?: 0)
        }

        val (_, argumentCount, getEntry) = V3.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)

        if (params.size == argumentCount || argumentCount == -1)
            return getEntry(opCode, params.toIntArray())
        return UnknownEntry(opCode, params.toIntArray())
    }
}