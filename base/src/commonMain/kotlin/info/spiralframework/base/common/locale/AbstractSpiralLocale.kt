package info.spiralframework.base.common.locale

import info.spiralframework.base.binding.DefaultLocaleBundle
import info.spiralframework.base.common.properties.Observables

abstract class AbstractSpiralLocale {
    private val _localisationBundles: MutableList<LocaleBundle> = ArrayList()
    private val _englishBundles: MutableList<LocaleBundle> = ArrayList()

    val localisationBundles: List<LocaleBundle> = _localisationBundles
    val englishBundles: List<LocaleBundle> = _englishBundles

    var currentLocale: CommonLocale by Observables.newValue(CommonLocale.defaultLocale, this::changeLocale)

    abstract fun localise(msg: String): String
    abstract fun localise(msg: String, arg: Any): String
    abstract fun localise(msg: String, arg1: Any, arg2: Any): String
    abstract fun localise(msg: String, vararg args: Any): String
    abstract fun localiseArray(msg: String, args: Array<out Any>): String

    abstract fun localiseEnglish(msg: String): String
    abstract fun localiseEnglish(msg: String, arg: Any): String
    abstract fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String
    abstract fun localiseEnglish(msg: String, vararg args: Any): String
    abstract fun localiseEnglishArray(msg: String, args: Array<out Any>): String

    fun addBundle(bundleName: String) {
        _localisationBundles.add(DefaultLocaleBundle(bundleName, currentLocale))
        _englishBundles.add(DefaultLocaleBundle(bundleName, CommonLocale.ENGLISH))
    }
    fun addBundle(localeBundle: LocaleBundle) {
        _localisationBundles.add(localeBundle)
    }
    fun addBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {
        _localisationBundles.add(localeBundle)
        _englishBundles.add(englishBundle)
    }
    fun addEnglishBundle(englishBundle: LocaleBundle) {
        _englishBundles.add(englishBundle)
    }

    fun removeBundle(bundleName: String) {
        _localisationBundles.removeAll { bundle -> bundle.bundleName == bundleName }
        _englishBundles.removeAll { bundle -> bundle.bundleName == bundleName }
    }
    fun removeBundle(localeBundle: LocaleBundle) {
        _localisationBundles.remove(localeBundle)
    }
    fun removeBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {
        _localisationBundles.remove(localeBundle)
        _englishBundles.remove(englishBundle)
    }
    fun removeEnglishBundle(englishBundle: LocaleBundle) {
        _englishBundles.remove(englishBundle)
    }

    fun changeLocale(locale: CommonLocale) {
        val oldArray = localisationBundles.toTypedArray()
        _localisationBundles.clear()
        _localisationBundles.addAll(oldArray.mapNotNull { bundle -> bundle.loadWithLocale(locale) })
    }
}