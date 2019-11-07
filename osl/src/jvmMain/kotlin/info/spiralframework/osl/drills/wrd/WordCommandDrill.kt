package info.spiralframework.osl.drills.wrd

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.WordScriptCommand
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.scripting.EnumWordScriptCommand
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordCommandDrill : DrillHead<WordScriptCommand> {
    val cmd = "WRD-COMMAND"

    override val klass: KClass<WordScriptCommand> = WordScriptCommand::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Word Command",
                    InlineWhitespace(),
                    FirstOf(EnumWordScriptCommand.values().map { enum -> enum.name }.toTypedArray()),
                    pushDrillHead(cmd, this@WordCommandDrill),
                    pushTmpAction(cmd),
                    AnyOf(":|"),
                    OptionalInlineWhitespace(),
                    Parameter(cmd),
                    operateOnTmpActions(cmd) { stack -> operate(this, stack.drop(1).toTypedArray()) },
                    pushTmpStack(cmd)
            )

    @Suppress("UNCHECKED_CAST")
    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptCommand? {
        val name = rawParams[0].toString()
        val scriptCommand = EnumWordScriptCommand.values().firstOrNull { enum -> enum.name.equals(name, true) } ?: return null
        val command = rawParams[1].toString()

        when (scriptCommand) {
            EnumWordScriptCommand.LABEL -> if (command !in parser.wordScriptLabels) parser.wordScriptLabels.add(command)
            EnumWordScriptCommand.PARAMETER -> if (command !in parser.wordScriptParameters) parser.wordScriptParameters.add(command)
            EnumWordScriptCommand.STRING -> if (command !in parser.wordScriptStrings) parser.wordScriptStrings.add(command)
            EnumWordScriptCommand.RAW -> {}
        }

        return WordScriptCommand(scriptCommand, command)
    }
}
