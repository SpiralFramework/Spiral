package info.spiralframework.osl.drills.wrd

import info.spiralframework.osl.LineCodeMatcher
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.v3.V3
import info.spiralframework.formats.scripting.EnumWordScriptCommand
import info.spiralframework.formats.scripting.wrd.WrdScript
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
                    pushDrillHead(cmd, this@NamedWrdSpiralDrill),
                    pushTmpAction(cmd),
                    Optional(
                            '|'
                    ),
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
                        val opName = stack[1].toString()
                        val opCode = V3.opCodes.entries.first { (_, triple) -> opName in triple.first }.key
                        val commandEnums = V3.opCodeCommandEntries[opCode]

                        val wrdParams = stack.drop(2).map(Any::toString)

                        wrdParams.forEachIndexed { index, param ->
                            if (param.startsWith("LABEL:")) {
                                val label = param.substringAfter("LABEL:")
                                ensureString(label, EnumWordScriptCommand.LABEL)
                            } else if (param.startsWith("PARAMETER:")) {
                                val parameter = param.substringAfter("PARAMETER:")
                                ensureString(parameter, EnumWordScriptCommand.PARAMETER)
                            } else if (param.startsWith("STRING:")) {
                                val string = param.substringAfter("STRING:")
                                ensureString(string, EnumWordScriptCommand.STRING)
                            } else if (!param.startsWith("RAW:") && commandEnums != null && index < commandEnums.size) {
                                ensureString(param, commandEnums[index])
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
