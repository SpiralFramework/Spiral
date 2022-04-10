@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.locale

import dev.brella.kornea.errors.common.KORNEA_ERROR_NOT_ENOUGH_DATA
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.errors.common.korneaNotFound
import info.spiralframework.base.common.io.SpiralResourceLoader
import kotlin.reflect.KClass

public interface SpiralLocale {
    public object NoOp : SpiralLocale {
        override fun localise(msg: String): String = msg
        override fun localise(msg: String, arg: Any): String = msg
        override fun localise(msg: String, arg1: Any, arg2: Any): String = msg
        override fun localise(msg: String, vararg args: Any): String = msg
        override fun localiseArray(msg: String, args: Array<out Any>): String = msg

        override suspend fun SpiralResourceLoader.addBundle(
            bundleName: String,
            context: KClass<*>
        ): KorneaResult<LocaleBundle> = korneaNotFound("Could not add bundle $bundleName (NoOp Locale)")

        override fun addBundle(localeBundle: LocaleBundle) {}

        override fun removeBundle(bundleName: String) {}
        override fun removeBundle(localeBundle: LocaleBundle) {}

        override suspend fun SpiralResourceLoader.changeLocale(locale: CommonLocale) {}
        override fun currentLocale(): CommonLocale? = null
    }

    public fun localise(msg: String): String
    public fun localise(msg: String, arg: Any): String
    public fun localise(msg: String, arg1: Any, arg2: Any): String
    public fun localise(msg: String, vararg args: Any): String
    public fun localiseArray(msg: String, args: Array<out Any>): String

    public suspend fun SpiralResourceLoader.addBundle(
        bundleName: String,
        context: KClass<*>
    ): KorneaResult<LocaleBundle>

    public fun addBundle(localeBundle: LocaleBundle)

    public fun removeBundle(bundleName: String)
    public fun removeBundle(localeBundle: LocaleBundle)

    public suspend fun SpiralResourceLoader.changeLocale(locale: CommonLocale)

    public fun currentLocale(): CommonLocale?
}

public abstract class AbstractSpiralLocale : SpiralLocale {
    private val _localisationBundles: MutableList<LocaleBundle> = ArrayList()
    private val _englishBundles: MutableList<LocaleBundle> = ArrayList()

    public val localisationBundles: List<LocaleBundle> = _localisationBundles
    public val englishBundles: List<LocaleBundle> = _englishBundles

    private var currentLocale: CommonLocale = CommonLocale.defaultLocale

    override suspend fun SpiralResourceLoader.addBundle(
        bundleName: String,
        context: KClass<*>
    ): KorneaResult<LocaleBundle> =
        CommonLocaleBundle.load(this, bundleName, currentLocale, context)
            .doOnSuccess(_localisationBundles::add)

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

public suspend inline fun <reified T : Any> SpiralLocale.loadBundle(
    resourceLoader: SpiralResourceLoader,
    bundleName: String
): KorneaResult<LocaleBundle> =
    resourceLoader.addBundle(bundleName, T::class)

public suspend inline fun <reified T : Any, B> B.loadTestBundle(bundleName: String): KorneaResult<LocaleBundle> where B : SpiralLocale, B : SpiralResourceLoader =
    this.addBundle(bundleName, T::class) //(this as SpiralResourceLoader)

public suspend fun SpiralLocale.loadBundle(
    resourceLoader: SpiralResourceLoader,
    bundleName: String,
    context: KClass<*>
): KorneaResult<LocaleBundle> =
    resourceLoader.addBundle(bundleName, context)

public suspend fun SpiralLocale.changeLocale(resourceLoader: SpiralResourceLoader, locale: CommonLocale): Unit =
    resourceLoader.changeLocale(locale)

public inline fun <reified T> SpiralLocale.localisedNotEnoughData(message: String): KorneaResult<T> =
    KorneaResult.errorAsIllegalState(KORNEA_ERROR_NOT_ENOUGH_DATA, localise(message))

public inline fun SpiralLocale.localiseOrNull(msg: String): String? = localise(msg).takeUnless { it == msg }
public inline fun SpiralLocale.localiseOrNull(msg: String, arg: Any): String? =
    localise(msg, arg).takeUnless { it == msg }

public inline fun SpiralLocale.localiseOrNull(msg: String, arg1: Any, arg2: Any): String? =
    localise(msg, arg1, arg2).takeUnless { it == msg }

public inline fun SpiralLocale.localiseOrNull(msg: String, vararg args: Any): String? =
    localiseArray(msg, args).takeUnless { it == msg }

public inline fun SpiralLocale.localiseArrayOrNull(msg: String, args: Array<out Any>): String? =
    localiseArray(msg, args).takeUnless { it == msg }

public inline fun SpiralLocale.localiseOrElse(msg: String, default: () -> Any): String =
    localise(msg).takeUnless { it == msg } ?: default().toString()

public inline fun SpiralLocale.localiseOrElse(msg: String, arg: Any, default: () -> Any): String =
    localise(msg, arg).takeUnless { it == msg } ?: default().toString()

public inline fun SpiralLocale.localiseOrElse(msg: String, arg1: Any, arg2: Any, default: () -> Any): String =
    localise(msg, arg1, arg2).takeUnless { it == msg } ?: default().toString()

public inline fun SpiralLocale.localiseOrElse(msg: String, vararg args: Any, default: () -> Any): String =
    localiseArray(msg, args).takeUnless { it == msg } ?: default().toString()

public inline fun SpiralLocale.localiseArrayOrElse(msg: String, args: Array<out Any>, default: () -> Any): String =
    localiseArray(msg, args).takeUnless { it == msg } ?: default().toString()

public inline fun <T> SpiralLocale.errorAsLocalisedIllegalArgument(
    errorCode: Int,
    errorMessage: String,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalArgument(
        errorCode,
        localiseOrNull(errorMessage) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedIllegalArgument(
    errorCode: Int,
    errorMessage: String,
    arg: Any,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalArgument(
        errorCode,
        localiseOrNull(errorMessage, arg) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedIllegalArgument(
    errorCode: Int,
    errorMessage: String,
    arg1: Any,
    arg2: Any,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalArgument(
        errorCode,
        localiseOrNull(errorMessage, arg1, arg2) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedIllegalArgument(
    errorCode: Int,
    errorMessage: String,
    vararg args: Any,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalArgument(
        errorCode,
        localiseArrayOrNull(errorMessage, args) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedArrayIllegalArgument(
    errorCode: Int,
    errorMessage: String,
    args: Array<out Any>,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalArgument(
        errorCode,
        localiseArrayOrNull(errorMessage, args) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )



public inline fun <T> SpiralLocale.errorAsLocalisedIllegalState(
    errorCode: Int,
    errorMessage: String,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalState(
        errorCode,
        localiseOrNull(errorMessage) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedIllegalState(
    errorCode: Int,
    errorMessage: String,
    arg: Any,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalState(
        errorCode,
        localiseOrNull(errorMessage, arg) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedIllegalState(
    errorCode: Int,
    errorMessage: String,
    arg1: Any,
    arg2: Any,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalState(
        errorCode,
        localiseOrNull(errorMessage, arg1, arg2) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedIllegalState(
    errorCode: Int,
    errorMessage: String,
    vararg args: Any,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalState(
        errorCode,
        localiseArrayOrNull(errorMessage, args) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )

public inline fun <T> SpiralLocale.errorAsLocalisedArrayIllegalState(
    errorCode: Int,
    errorMessage: String,
    args: Array<out Any>,
    cause: KorneaResult.Failure? = null,
    generateStacktraceUponCreation: Boolean = KorneaResult.WithErrorDetails.DEFAULT_GENERATE_STACKTRACE_ON_CREATION,
    includeResultCodeInError: Boolean = KorneaResult.WithErrorDetails.DEFAULT_INCLUDE_RESULT_CODE_IN_ERROR
): KorneaResult<T> =
    KorneaResult.errorAsIllegalState(
        errorCode,
        localiseArrayOrNull(errorMessage, args) ?: errorMessage,
        cause,
        generateStacktraceUponCreation,
        includeResultCodeInError
    )