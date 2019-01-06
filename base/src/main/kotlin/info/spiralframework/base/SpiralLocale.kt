package info.spiralframework.base

import java.text.MessageFormat
import java.util.*

object SpiralLocale {
    val _localisationBundles: MutableList<ResourceBundle> = ArrayList()
    val localisationBundles: List<ResourceBundle> = _localisationBundles

    val _englishBundles: MutableList<ResourceBundle> = ArrayList()
    val englishBundles: List<ResourceBundle> = _englishBundles

    fun localise(base: String, vararg values: Any?): String {
        val msg = localisationBundles.firstOrNull { bundle -> bundle.containsKey(base) }?.getString(base) ?: base
        return MessageFormat.format(msg, *values)
    }

    fun localiseString(base: String): String = localisationBundles.firstOrNull { bundle -> bundle.containsKey(base) }?.getString(base) ?: base

    fun localiseForEnglish(base: String, vararg values: Any?): String {
        val msg = englishBundles.firstOrNull { bundle -> bundle.containsKey(base) }?.getString(base) ?: base
        return MessageFormat.format(msg, *values)
    }

    fun changeLanguage(locale: Locale) {
        val oldArray = localisationBundles.toTypedArray()
        _localisationBundles.clear()
        _localisationBundles.addAll(oldArray.map { bundle -> ResourceBundle.getBundle(bundle.baseBundleName, locale) })
    }

    fun addBundle(bundleName: String) {
        _localisationBundles.add(ResourceBundle.getBundle(bundleName))
        _englishBundles.add(ResourceBundle.getBundle(bundleName, Locale.ENGLISH))
    }
}