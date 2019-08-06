package info.spiralframework.base.locale

import java.text.MessageFormat
import java.util.*
import kotlin.properties.Delegates

object SpiralLocale {
    val _localisationBundles: MutableList<ResourceBundle> = ArrayList()
    val localisationBundles: List<ResourceBundle> = _localisationBundles

    val _englishBundles: MutableList<ResourceBundle> = ArrayList()
    val englishBundles: List<ResourceBundle> = _englishBundles

    var currentLocale: Locale by Delegates.observable(Locale.getDefault()) { _, _, new -> changeLocale(new) }

    val PROMPT_AFFIRMATIVE: String
        get() = localiseString("base.prompt.affirmative")
    val PROMPT_NEGATIVE: String
        get() = localiseString("base.prompt.negative")

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

    fun changeLocale(locale: Locale) {
        val oldArray = localisationBundles.toTypedArray()
        _localisationBundles.clear()
        _localisationBundles.addAll(oldArray.mapNotNull { bundle ->
            (bundle as? CustomLocaleBundle)?.loadWithLocale(locale)
                    ?: ResourceBundle.getBundle(bundle.baseBundleName, locale)
        })
    }

    fun addBundle(bundleName: String) {
        _localisationBundles.add(ResourceBundle.getBundle(bundleName, currentLocale))
        _englishBundles.add(ResourceBundle.getBundle(bundleName, Locale.ENGLISH))
    }

    fun addBundle(loader: (Locale) -> Pair<ResourceBundle, ResourceBundle>?) {
        loader(currentLocale)?.let { (current, english) ->
            _localisationBundles.add(current)
            _englishBundles.add(english)
        }
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