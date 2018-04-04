package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptCommand
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordCommandDrill: DrillHead<WordScriptCommand> {
    val cmd = "WRD-COMMAND"
    val VALID = intArrayOf(1, 2, 3)

    override val klass: KClass<WordScriptCommand> = WordScriptCommand::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Word Command",
                    Whitespace(),
                    Digit(),
                    Action<Any> { (match().toIntOrNull() ?: 1) in VALID },
                    pushTmpAction(cmd, this@WordCommandDrill),
                    pushTmpAction(cmd),
                    ':',
                    ZeroOrMore(Whitespace()),
                    Parameter(cmd),
                    operateOnTmpActions(cmd) { stack -> operate(this, stack.drop(1).toTypedArray()) },
                    pushTmpStack(cmd)
            )

    @Suppress("UNCHECKED_CAST")
    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptCommand? {
        val num = rawParams[0].toString().toIntOrNull() ?: 1
        if (num !in VALID)
            return null

        val enum = EnumWordScriptCommand.values()[num - 1]
        val existing = parser.data["wrd-command-$enum"] as? MutableList<String> ?: ArrayList<String>()
        existing.add(rawParams[1].toString())
        parser.data["wrd-command-$enum"] = existing

        return WordScriptCommand(num, rawParams[1].toString())
    }
}