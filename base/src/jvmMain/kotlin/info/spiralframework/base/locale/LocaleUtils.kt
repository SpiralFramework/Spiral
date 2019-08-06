package info.spiralframework.base.locale

import info.spiralframework.base.binding.SpiralLocale
import info.spiralframework.base.common.locale.promptAffirmative
import info.spiralframework.base.common.locale.promptNegative
import info.spiralframework.base.common.locale.promptShortAffirmative

const val AFFIRMATIVE = true
const val NEGATIVE = false

fun SpiralLocale.readConfirmation(defaultToAffirmative: Boolean = true): Boolean {
    val affirmative = promptAffirmative()

    val input = readLine()?.trim()?.takeIf(String::isNotBlank)
            ?: if (defaultToAffirmative) affirmative else promptNegative()

    return if (input.equals(input, true) || input.startsWith(promptShortAffirmative(), true)) AFFIRMATIVE else NEGATIVE
}
