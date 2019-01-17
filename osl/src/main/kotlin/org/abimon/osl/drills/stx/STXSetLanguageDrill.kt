package org.abimon.osl.drills.stx

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.text.STXT
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object STXSetLanguageDrill: DrillHead<STXT.Language> {
    val cmd = "STX-SET-LANGUAGE"

    val languageNames = STXT.Language.values().map { lang -> lang.name.toUpperCase() }

    override val klass: KClass<STXT.Language> = STXT.Language::class
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): STXT.Language {
        val langName = rawParams[0].toString()

        return STXT.Language.values().firstOrNull { lang -> lang.name.equals(langName, true) } ?: STXT.Language.UNK
    }
}