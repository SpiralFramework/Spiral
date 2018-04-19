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
                    pushTmpAction(cmd, this@BasicWrdSpiralDrill),
                    pushTmpAction(cmd),
                    Optional(
                            '|'
                    ),
                    Optional(
                            ParamList(
                                    cmd,
                                    Parameter(cmd),
                                    Sequence(
                                            ',',
                                            OptionalWhitespace()
                                    )
                            )
                    ),
                    operateOnTmpActions(cmd) { stack ->
                        val opCode = "${stack[1]}".toInt(16)
                        val wrdParams = stack.drop(2).map(Any::toString)
                        val params = ArrayList<Int>()

                        val commandEnum = V3.opCodeCommandEntries[opCode] ?: EnumWordScriptCommand.TWO
                        val commands = data["wrd-command-$commandEnum"] as? MutableList<String> ?: ArrayList<String>()

                        for (param in wrdParams) {
                            if (!param.startsWith("raw:")) {
                                if (param !in commands) {
                                    commands.add(param)
                                    push(listOf(WordCommandDrill, commandEnum.ordinal + 1, param))
                                }
                            }
                        }

                        data["wrd-command-$commandEnum"] = commands
                    },
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript = formScript(parser, rawParams)

    @Suppress("UNCHECKED_CAST")
    fun formScript(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WrdScript {
        val opCode = "${rawParams[0]}".toInt(16)
        val wrdParams = rawParams.copyOfRange(1, rawParams.size).map(Any::toString)
        val params = ArrayList<Int>()

        val commandEnum = V3.opCodeCommandEntries[opCode] ?: EnumWordScriptCommand.TWO
        val commands = parser.data["wrd-command-$commandEnum"] as? List<String> ?: ArrayList<String>()

        for (param in wrdParams) {
            if (param.startsWith("raw:"))
                params.add(param.substring(4).toIntOrNull() ?: 0)
            else if (param in commands) {
                val index = commands.indexOf(param)
                params.add(index shr 8, index and 0xFF)
            } else {
                error("$param is not in $commands; something has gone horribly wrong")
            }
        }

        val (_, argumentCount, getEntry) = V3.opCodes[opCode] ?: (null to -1 and ::UnknownEntry)

        if (params.size == argumentCount || argumentCount == -1)
            return getEntry(opCode, params.toIntArray())
        return UnknownEntry(opCode, params.toIntArray())
    }
}