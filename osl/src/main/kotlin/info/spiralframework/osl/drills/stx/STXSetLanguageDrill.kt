package info.spiralframework.osl.drills.stx

import info.spiralframework.formats.text.STX
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object STXSetLanguageDrill: DrillHead<STX.Language> {
    val cmd = "STX-SET-LANGUAGE"

    val languageNames = STX.Language.values().map { lang -> lang.name.toUpperCase() }

    override val klass: KClass<STX.Language> = STX.Language::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf("Language:", "Set Language:", "Language Is ", "Set Language To "),
                            pushDrillHead(cmd, this@STXSetLanguageDrill),
                            OptionalInlineWhitespace(),
                            Parameter(cmd),
                            Action<Any> { tmpStack[cmd]?.peek()?.toString()?.toUpperCase() in languageNames }
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): STX.Language {
        val langName = rawParams[0].toString()

        return STX.Language.values().firstOrNull { lang -> lang.name.equals(langName, true) } ?: STX.Language.UNK
    }
}
