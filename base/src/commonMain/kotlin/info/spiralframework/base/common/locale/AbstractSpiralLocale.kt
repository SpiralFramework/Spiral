package info.spiralframework.base.common.locale

import kotlin.reflect.KClass

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

        override suspend fun addBundle(bundleName: String, context: KClass<*>) {}
        override fun addBundle(localeBundle: LocaleBundle) {}
        override fun addBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {}
        override fun addEnglishBundle(englishBundle: LocaleBundle) {}

        override fun removeBundle(bundleName: String) {}
        override fun removeBundle(localeBundle: LocaleBundle) {}
        override fun removeBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle) {}
        override fun removeEnglishBundle(englishBundle: LocaleBundle) {}

        override suspend fun changeLocale(locale: CommonLocale) {}
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

    suspend fun addBundle(bundleName: String, context: KClass<*>)
    fun addBundle(localeBundle: LocaleBundle)
    fun addBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle)
    fun addEnglishBundle(englishBundle: LocaleBundle)

    fun removeBundle(bundleName: String)
    fun removeBundle(localeBundle: LocaleBundle)
    fun removeBundle(localeBundle: LocaleBundle, englishBundle: LocaleBundle)
    fun removeEnglishBundle(englishBundle: LocaleBundle)

    suspend fun changeLocale(locale: CommonLocale)
}

abstract class AbstractSpiralLocale: SpiralLocale {
    private val _localisationBundles: MutableList<LocaleBundle> = ArrayList()
    private val _englishBundles: MutableList<LocaleBundle> = ArrayList()

    val localisationBundles: List<LocaleBundle> = _localisationBundles
    val englishBundles: List<LocaleBundle> = _englishBundles

    private var currentLocale: CommonLocale = CommonLocale.defaultLocale

    @ExperimentalUnsignedTypes
    override suspend fun addBundle(bundleName: String, context: KClass<*>) {
        _localisationBundles.add(requireNotNull(CommonLocaleBundle.load(bundleName, currentLocale, context)))
        _englishBundles.add(requireNotNull(CommonLocaleBundle.load(bundleName, CommonLocale.ENGLISH, context)))
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

    override suspend fun changeLocale(locale: CommonLocale) {
        val oldArray = localisationBundles.toTypedArray()
        _localisationBundles.clear()
        _localisationBundles.addAll(oldArray.mapNotNull { bundle -> bundle.loadWithLocale(locale) })

        this.currentLocale = locale
    }
}

suspend inline fun <reified T: Any> SpiralLocale.addBundle(bundleName: String) = addBundle(bundleName, T::class)