package info.spiralframework.base

import java.text.MessageFormat
import java.util.*

object SpiralLocale {
    val _localisationBundles: MutableList<ResourceBundle> = ArrayList()
    val localisationBundles: List<ResourceBundle> = _localisationBundles

    val _englishBundles: MutableList<ResourceBundle> = ArrayList()
    val englishBundles: List<ResourceBundle> = _englishBundles

    val PROMPT_AFFIRMATIVE: String
        get() = localiseString("base.prompt.affirmative")
    val PROMPT_NEGATIVE: String
        get() = localiseString("base.prompt.negative")
    val PROMPT_EXIT: String
        get() = localiseString("base.prompt.exit")

    fun localise(base: String, vararg values: Any?): String {
        val msg = localisationBundles.firstOrNull { bundle -> bundle.containsKey(base) }?.getString(base) ?: base
        return MessageFormat.format(msg, *values)
    }

    fun localiseString(base: String): String = localisationBundles.firstOrNull { bundle -> bundle.containsKey(base) }?.getString(base)
            ?: base

    fun localiseForEnglish(base: String, vararg values: Any?): String {
        val msg = englishBundles.firstOrNull { bundle -> bundle.containsKey(base) }?.getString(base) ?: base
        return MessageFormat.format(msg, *values)
    }

    fun changeLanguage(locale: Locale) {
        Locale.setDefault(locale)
        val oldArray = localisationBundles.toTypedArray()
        _localisationBundles.clear()
        _localisationBundles.addAll(oldArray.map { bundle -> ResourceBundle.getBundle(bundle.baseBundleName, locale) })
    }

    fun addBundle(bundleName: String) {
        _localisationBundles.add(ResourceBundle.getBundle(bundleName))
        _englishBundles.add(ResourceBundle.getBundle(bundleName, Locale.ENGLISH))
    }
    fun removeBundle(bundleName: String) {
        _localisationBundles.removeIf { bundle -> bundle.baseBundleName == bundleName }
        _englishBundles.removeIf { bundle -> bundle.baseBundleName == bundleName }
    }

    fun readConfirmation(defaultToAffirmative: Boolean = true): Boolean =
            (readLine()?.trim()?.takeIf(String::isNotBlank)
                    ?: (if (defaultToAffirmative) PROMPT_AFFIRMATIVE else PROMPT_NEGATIVE)).toLowerCase()[0] == PROMPT_AFFIRMATIVE[0]

    init {
        addBundle("SpiralBase")
    }
}