package org.abimon.osl.drills.wrd

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.WordScriptString
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.EnumWordScriptCommand
import org.parboiled.Rule
import kotlin.reflect.KClass

object WordStringDrill: DrillHead<WordScriptString> {
    val cmd = "WRD-STRING"
    val VALID = intArrayOf(1, 2, 3)

    override val klass: KClass<WordScriptString> = WordScriptString::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    "Word String:",
                    pushTmpAction(cmd, this@WordStringDrill),
                    ZeroOrMore(Whitespace()),
                    Parameter(cmd),
                    operateOnTmpActions(cmd) { stack -> operate(this, stack.drop(1).toTypedArray()) },
                    pushTmpStack(cmd)
            )

    @Suppress("UNCHECKED_CAST")
    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): WordScriptString {
        val existing = parser.data["wrd-${EnumWordScriptCommand.STRING}"] as? MutableList<String> ?: ArrayList<String>()
        existing.add(rawParams[0].toString())
        parser.data["wrd-command-${EnumWordScriptCommand.STRING}"] = existing

        return WordScriptString(rawParams[0].toString())
    }
}