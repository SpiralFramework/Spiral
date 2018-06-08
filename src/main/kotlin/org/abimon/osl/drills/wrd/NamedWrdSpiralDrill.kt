package org.abimon.osl.drills.wrd

import org.abimon.osl.LineCodeMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object NamedWrdSpiralDrill : DrillHead<WrdScript> {
    val cmd = "NAMED-WRD"

    override val klass: KClass<WrdScript> = WrdScript::class
    @Suppress("UNCHECKED_CAST")
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    OneOrMore(LineCodeMatcher),
                    Action<Any> {
                        val name = match()
                        V3.opCodes.values.any { (names) -> name in names }
                    },
                    pushTmpAction(cmd, this@NamedWrdSpiralDrill),
                    pushTmpAction(cmd),
                    Optional(
                            '|'
                    ),
                    Optional(
                            ParamList(
                                    cmd,
                                    Parameter(BasicWrdSpiralDrill.cmd),
                                    Sequence(
                                            ',',
                                            OptionalWhitespace()
                                    )
                            )
                    ),
                    operateOnTmpActions(BasicWrdSpiralDrill.cmd) { stack ->
                        val wrdParams = stack.drop(2).map(Any::toString)

                        for (param in wrdParams) {
                            if (param.startsWith("LABEL|")) {
                                val label = param.substringAfter("LABEL|")
                                if (label !in wordScriptLabels) {
                                    wordScriptLabels.add(label)
                                    push(listOf(WordCommandDrill, EnumWordScriptCommand.LABEL, label))
                                }
                            } else if (param.startsWith("PARAMETER|")) {
                                val parameter = param.substringAfter("PARAMETER|")
                                if (parameter !in wordScriptParameters) {
                                    wordScriptParameters.add(parameter)
                                    push(listOf(WordCommandDrill, EnumWordScriptCommand.PARAMETER, parameter))
                                }
                            } else if (param.startsWith("STRING|")) {
                                val string = param.substringAfter("STRING|")
                                if (string !in wordScriptStrings) {
                                    wordScriptStrings.add(string)
                                    push(listOf(WordCommandDrill, EnumWordScriptCommand.STRING, string))
                                }
                            }
                        }
                    },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript {
        val opName = rawParams[0].toString()
        rawParams[0] = V3.opCodes.entries.first { (_, triple) -> opName in triple.first }.key.toString(16)
        return BasicWrdSpiralDrill.formScript(parser, rawParams)
    }
}