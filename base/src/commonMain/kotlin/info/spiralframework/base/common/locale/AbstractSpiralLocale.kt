package info.spiralframework.base.common.locale

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.SpiralResourceLoader
import dev.brella.kornea.errors.common.*
import kotlin.reflect.KClass

interface SpiralLocale {
    object NoOp: SpiralLocale {
        override fun localise(msg: String): String = msg
        override fun localise(msg: String, arg: Any): String = msg
        override fun localise(msg: String, arg1: Any, arg2: Any): String = msg
        override fun localise(msg: String, vararg args: Any): String = msg
        override fun localiseArray(msg: String, args: Array<out Any>): String = msg

        override suspend fun SpiralResourceLoader.addBundle(bundleName: String, context: KClass<*>): KorneaResult<LocaleBundle> = korneaNotFound("Could not add bundle $bundleName (NoOp Locale)")
        override fun addBundle(localeBundle: LocaleBundle) {}

        override fun removeBundle(bundleName: String) {}
        override fun removeBundle(localeBundle: LocaleBundle) {}

        override suspend fun SpiralResourceLoader.changeLocale(locale: CommonLocale) {}
        override fun currentLocale(): CommonLocale? = null
    }

    fun localise(msg: String): String
    fun localise(msg: String, arg: Any): String
    fun localise(msg: String, arg1: Any, arg2: Any): String
    fun localise(msg: String, vararg args: Any): String
    fun localiseArray(msg: String, args: Array<out Any>): String

    suspend fun SpiralResourceLoader.addBundle(bundleName: String, context: KClass<*>): KorneaResult<LocaleBundle>
    fun addBundle(localeBundle: LocaleBundle)

    fun removeBundle(bundleName: String)
    fun removeBundle(localeBundle: LocaleBundle)

    suspend fun SpiralResourceLoader.changeLocale(locale: CommonLocale)

    fun currentLocale(): CommonLocale?
}

abstract class AbstractSpiralLocale: SpiralLocale {
    private val _localisationBundles: MutableList<LocaleBundle> = ArrayList()
    private val _englishBundles: MutableList<LocaleBundle> = ArrayList()

    val localisationBundles: List<LocaleBundle> = _localisationBundles
    val englishBundles: List<LocaleBundle> = _englishBundles

    private var currentLocale: CommonLocale = CommonLocale.defaultLocale

    @ExperimentalUnsignedTypes
    override suspend fun SpiralResourceLoader.addBundle(bundleName: String, context: KClass<*>): KorneaResult<LocaleBundle> {
        val bundle = CommonLocaleBundle.load(this, bundleName, currentLocale, context)
        if (bundle is KorneaResult.Success) {
            _localisationBundles.add(bundle.get())
        }

        return bundle.cast()
    }
    override fun addBundle(localeBundle: LocaleBundle) {
        _localisationBundles.add(localeBundle)
    }

    override fun removeBundle(bundleName: String) {
        _localisationBundles.removeAll { bundle -> bundle.bundleName == bundleName }
        _englishBundles.removeAll { bundle -> bundle.bundleName == bundleName }
    }
    override fun removeBundle(localeBundle: LocaleBundle) {
        _localisationBundles.remove(localeBundle)
    }

    override suspend fun SpiralResourceLoader.changeLocale(locale: CommonLocale) {
        val oldArray = localisationBundles.toTypedArray()
        _localisationBundles.clear()
        _localisationBundles.addAll(oldArray.mapNotNull { bundle -> bundle.loadWithLocale(this, locale).getOrNull() })

        this@AbstractSpiralLocale.currentLocale = locale
    }

    override fun currentLocale(): CommonLocale = currentLocale
}

suspend inline fun <reified T: Any> SpiralLocale.loadBundle(resourceLoader: SpiralResourceLoader, bundleName: String) = resourceLoader.addBundle(bundleName, T::class)
suspend inline fun <reified T: Any, B> B.loadTestBundle(bundleName: String) where B: SpiralLocale, B: SpiralContext = (this as SpiralResourceLoader).addBundle(bundleName, T::class)

suspend fun SpiralLocale.loadBundle(resourceLoader: SpiralResourceLoader, bundleName: String, context: KClass<*>) = resourceLoader.addBundle(bundleName, context)
suspend fun SpiralLocale.changeLocale(resourceLoader: SpiralResourceLoader, locale: CommonLocale) = resourceLoader.changeLocale(locale)

inline fun <reified T> SpiralLocale.localisedNotEnoughData(message: String): KorneaResult<T> =
        KorneaResult.errorAsIllegalState(KORNEA_ERROR_NOT_ENOUGH_DATA, localise(message))