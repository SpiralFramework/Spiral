package info.spiralframework.base.common.locale

import info.spiralframework.base.binding.DefaultLocaleBundle
import info.spiralframework.base.common.properties.Observables

interface SpiralLocale {
    object NoOp: SpiralLocale {
        override fun localise(msg: String): String = msg
        override fun localise(msg: String, arg: Any): String = msg
        override fun localise(msg: String, arg1: Any, arg2: Any): String = msg
        override fun localise(msg: String, vararg args: Any): String = msg
        override fun localiseArray(msg: String, args: Array<out Any>): String = msg
        override fun localiseEnglish(msg: String): String = msg
        override fun localiseEnglish(msg: String, arg: Any): String = msg
        override fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String = msg
        override fun localiseEnglish(msg: String, vararg args: Any): String = msg
        override fun localiseEnglishArray(msg: String, args: Array<out Any>): String = msg

        override fun addBundle(bundleName: String) {}
        override fun addBundle(localeBundle: LocaleBundle) {}
        override fun addBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {}
        override fun addEnglishBundle(englishBundle: LocaleBundle) {}

        override fun removeBundle(bundleName: String) {}
        override fun removeBundle(localeBundle: LocaleBundle) {}
        override fun removeBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {}
        override fun removeEnglishBundle(englishBundle: LocaleBundle) {}

        override fun changeLocale(locale: CommonLocale) {}
    }

    fun localise(msg: String): String
    fun localise(msg: String, arg: Any): String
    fun localise(msg: String, arg1: Any, arg2: Any): String
    fun localise(msg: String, vararg args: Any): String
    fun localiseArray(msg: String, args: Array<out Any>): String

    fun localiseEnglish(msg: String): String
    fun localiseEnglish(msg: String, arg: Any): String
    fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String
    fun localiseEnglish(msg: String, vararg args: Any): String
    fun localiseEnglishArray(msg: String, args: Array<out Any>): String

    fun addBundle(bundleName: String)
    fun addBundle(localeBundle: LocaleBundle)
    fun addBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle)
    fun addEnglishBundle(englishBundle: LocaleBundle)

    fun removeBundle(bundleName: String)
    fun removeBundle(localeBundle: LocaleBundle)
    fun removeBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle)
    fun removeEnglishBundle(englishBundle: LocaleBundle)

    fun changeLocale(locale: CommonLocale)
}

abstract class AbstractSpiralLocale: SpiralLocale {
    private val _localisationBundles: MutableList<LocaleBundle> = ArrayList()
    private val _englishBundles: MutableList<LocaleBundle> = ArrayList()

    val localisationBundles: List<LocaleBundle> = _localisationBundles
    val englishBundles: List<LocaleBundle> = _englishBundles

    var currentLocale: CommonLocale by Observables.newValue(CommonLocale.defaultLocale) { newValue -> changeLocale(newValue) }

    override fun addBundle(bundleName: String) {
        _localisationBundles.add(DefaultLocaleBundle(bundleName, currentLocale))
        _englishBundles.add(DefaultLocaleBundle(bundleName, CommonLocale.ENGLISH))
    }
    override fun addBundle(localeBundle: LocaleBundle) {
        _localisationBundles.add(localeBundle)
    }
    override fun addBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {
        _localisationBundles.add(localeBundle)
        _englishBundles.add(englishBundle)
    }
    override fun addEnglishBundle(englishBundle: LocaleBundle) {
        _englishBundles.add(englishBundle)
    }

    override fun removeBundle(bundleName: String) {
        _localisationBundles.removeAll { bundle -> bundle.bundleName == bundleName }
        _englishBundles.removeAll { bundle -> bundle.bundleName == bundleName }
    }
    override fun removeBundle(localeBundle: LocaleBundle) {
        _localisationBundles.remove(localeBundle)
    }
    override fun removeBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {
        _localisationBundles.remove(localeBundle)
        _englishBundles.remove(englishBundle)
    }
    override fun removeEnglishBundle(englishBundle: LocaleBundle) {
        _englishBundles.remove(englishBundle)
    }

    override fun changeLocale(locale: CommonLocale) {
        val oldArray = localisationBundles.toTypedArray()
        _localisationBundles.clear()
        _localisationBundles.addAll(oldArray.mapNotNull { bundle -> bundle.loadWithLocale(locale) })
    }
}