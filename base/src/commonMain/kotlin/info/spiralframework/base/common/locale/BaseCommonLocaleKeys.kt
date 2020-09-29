package info.spiralframework.base.common.locale

object BaseCommonLocaleKeys {
    const val PROMPT_AFFIRMATIVE = "base.prompt.affirmative"
    const val PROMPT_NEGATIVE = "base.prompt.negative"

    const val PROMPT_SHORT_AFFIRMATIVE = "base.prompt.short.affirmative"
    const val PROMPT_SHORT_NEGATIVE = "base.prompt.short.negative"

    const val PROMPT_EXIT = "base.prompt.exit"

    const val CONST_NULL = "base.const.null"
}

fun SpiralLocale.promptAffirmative(): String = localise(BaseCommonLocaleKeys.PROMPT_AFFIRMATIVE)
fun SpiralLocale.promptNegative(): String = localise(BaseCommonLocaleKeys.PROMPT_NEGATIVE)

fun SpiralLocale.promptShortAffirmative(): String = localise(BaseCommonLocaleKeys.PROMPT_SHORT_AFFIRMATIVE)
fun SpiralLocale.promptShortNegative(): String = localise(BaseCommonLocaleKeys.PROMPT_SHORT_NEGATIVE)

fun SpiralLocale.constNull(): String = localise(BaseCommonLocaleKeys.CONST_NULL)
fun SpiralLocale.promptExit(): List<String> = localise(BaseCommonLocaleKeys.PROMPT_EXIT).split(';')