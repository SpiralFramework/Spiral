package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptCommand
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordStringDrill : DrillHead<WordScriptCommand> {
    val cmd = "WRD-STRING"

    override val klass: KClass<WordScriptCommand> = WordScriptCommand::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Word String",
                            AnyOf(":|"),
                            pushDrillHead(cmd, this@WordStringDrill),
                            OptionalInlineWhitespace(),
                            WrdText(cmd),
                            operateOnTmpActions(cmd) { stack -> operate(this, stack.drop(1).toTypedArray()) }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptCommand {
        val string = rawParams[0].toString()
        parser.wordScriptStrings.add(string)
        return WordScriptCommand(EnumWordScriptCommand.STRING, string)
    }
}