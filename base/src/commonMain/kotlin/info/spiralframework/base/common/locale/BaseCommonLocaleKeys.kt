package info.spiralframework.base.common.locale

public object BaseCommonLocaleKeys {
    public const val PROMPT_AFFIRMATIVE: String = "base.prompt.affirmative"
    public const val PROMPT_NEGATIVE: String = "base.prompt.negative"

    public const val PROMPT_SHORT_AFFIRMATIVE: String = "base.prompt.short.affirmative"
    public const val PROMPT_SHORT_NEGATIVE: String = "base.prompt.short.negative"

    public const val PROMPT_EXIT: String = "base.prompt.exit"

    public const val CONST_NULL: String = "base.const.null"
}

public fun SpiralLocale.promptAffirmative(): String = localise(BaseCommonLocaleKeys.PROMPT_AFFIRMATIVE)
public fun SpiralLocale.promptNegative(): String = localise(BaseCommonLocaleKeys.PROMPT_NEGATIVE)

public fun SpiralLocale.promptShortAffirmative(): String = localise(BaseCommonLocaleKeys.PROMPT_SHORT_AFFIRMATIVE)
public fun SpiralLocale.promptShortNegative(): String = localise(BaseCommonLocaleKeys.PROMPT_SHORT_NEGATIVE)

public fun SpiralLocale.constNull(): String = localise(BaseCommonLocaleKeys.CONST_NULL)
public fun SpiralLocale.promptExit(): List<String> = localise(BaseCommonLocaleKeys.PROMPT_EXIT).split(';')