package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptCommand
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordCommandDrill : DrillHead<WordScriptCommand> {
    val cmd = "WRD-COMMAND"

    override val klass: KClass<WordScriptCommand> = WordScriptCommand::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Word Command",
                    Whitespace(),
                    ParameterToStack(),
                    Action<Any> {
                        val name = pop().toString()
                        push(name)
                        return@Action EnumWordScriptCommand.values().any { enum -> enum.name.equals(name, true) }
                    },
                    pushTmpAction(cmd, this@WordCommandDrill),
                    pushTmpFromStack(cmd),
                    AnyOf(":|"),
                    OptionalWhitespace(),
                    Parameter(cmd),
                    operateOnTmpActions(cmd)
                    { stack -> operate(this, stack.drop(1).toTypedArray()) },
                    pushTmpStack(cmd)
            )

    @Suppress("UNCHECKED_CAST")
    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptCommand? {
        val name = rawParams[0].toString()
        val scriptCommand = EnumWordScriptCommand.values().firstOrNull { enum -> enum.name.equals(name, true) } ?: return null

        val existing = parser.data["wrd-command-$scriptCommand"] as? MutableList<String> ?: ArrayList<String>()
        existing.add(rawParams[1].toString())
        parser.data["wrd-command-$scriptCommand"] = existing

        return WordScriptCommand(scriptCommand, rawParams[1].toString())
    }
}