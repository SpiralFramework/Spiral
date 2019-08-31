package info.spiralframework.base.binding

import info.spiralframework.base.common.locale.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.MessageFormat
import java.util.*

actual class SpiralLogger(val logger: Logger) : Logger by logger {
    actual constructor(name: String) : this(LoggerFactory.getLogger(name))

    actual override fun error(format: String) = if (isErrorEnabled) logger.error(localise(format)) else Unit
    actual override fun error(format: String, arg: Any) = if (isErrorEnabled) logger.error(localise(format, arg), arg) else Unit
    actual override fun error(format: String, th: Throwable) = if (isErrorEnabled) logger.error(localise(format), th) else Unit
    actual override fun error(format: String, arg1: Any, arg2: Any) = if (isErrorEnabled) logger.error(localise(format, arg1, arg2), arg2) else Unit
    actual override fun error(format: String, vararg args: Any) = if (isErrorEnabled) logger.error(localise(format, args), args.lastOrNull()) else Unit
    actual fun errorArray(format: String, args: Array<out Any>) = if (isErrorEnabled) logger.error(localise(format, args), args.lastOrNull()) else Unit

    actual override fun warn(format: String) = if (isWarnEnabled) logger.warn(localise(format)) else Unit
    actual override fun warn(format: String, arg: Any) = if (isWarnEnabled) logger.warn(localise(format, arg), arg) else Unit
    actual override fun warn(format: String, th: Throwable) = if (isWarnEnabled) logger.warn(localise(format), th) else Unit
    actual override fun warn(format: String, arg1: Any, arg2: Any) = if (isWarnEnabled) logger.warn(localise(format, arg1, arg2), arg2) else Unit
    actual override fun warn(format: String, vararg args: Any) = if (isWarnEnabled) logger.warn(localise(format, args), args.lastOrNull()) else Unit
    actual fun warnArray(format: String, args: Array<out Any>) = if (isWarnEnabled) logger.warn(localise(format, args), args.lastOrNull()) else Unit

    actual override fun info(format: String) = if (isInfoEnabled) logger.info(localise(format)) else Unit
    actual override fun info(format: String, arg: Any) = if (isInfoEnabled) logger.info(localise(format, arg), arg) else Unit
    actual override fun info(format: String, th: Throwable) = if (isInfoEnabled) logger.info(localise(format), th) else Unit
    actual override fun info(format: String, arg1: Any, arg2: Any) = if (isInfoEnabled) logger.info(localise(format, arg1, arg2), arg2) else Unit
    actual override fun info(format: String, vararg args: Any) = if (isInfoEnabled) logger.info(localise(format, args), args.lastOrNull()) else Unit
    actual fun infoArray(format: String, args: Array<out Any>) = if (isInfoEnabled) logger.info(localise(format, args), args.lastOrNull()) else Unit

    actual override fun debug(format: String) = if (isDebugEnabled) logger.debug(localise(format)) else Unit
    actual override fun debug(format: String, arg: Any) = if (isDebugEnabled) logger.debug(localise(format, arg), arg) else Unit
    actual override fun debug(format: String, th: Throwable) = if (isDebugEnabled) logger.debug(localise(format), th) else Unit
    actual override fun debug(format: String, arg1: Any, arg2: Any) = if (isDebugEnabled) logger.debug(localise(format, arg1, arg2), arg2) else Unit
    actual override fun debug(format: String, vararg args: Any) = if (isDebugEnabled) logger.debug(localise(format, args), args.lastOrNull()) else Unit
    actual fun debugArray(format: String, args: Array<out Any>) = if (isDebugEnabled) logger.debug(localise(format, args), args.lastOrNull()) else Unit

    actual override fun trace(format: String) = if (isTraceEnabled) logger.trace(localise(format)) else Unit
    actual override fun trace(format: String, arg: Any) = if (isTraceEnabled) logger.trace(localise(format, arg), arg) else Unit
    actual override fun trace(format: String, th: Throwable) = if (isTraceEnabled) logger.trace(localise(format), th) else Unit
    actual override fun trace(format: String, arg1: Any, arg2: Any) = if (isTraceEnabled) logger.trace(localise(format, arg1, arg2), arg2) else Unit
    actual override fun trace(format: String, vararg args: Any) = if (isTraceEnabled) logger.trace(localise(format, args), args.lastOrNull()) else Unit
    actual fun traceArray(format: String, args: Array<out Any>) = if (isTraceEnabled) logger.trace(localise(format, args), args.lastOrNull()) else Unit
}

actual object SpiralLocale : AbstractSpiralLocale() {
    actual override fun localise(msg: String): String =
            localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg

    actual override fun localise(msg: String, arg: Any): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, arg)
    }

    actual override fun localise(msg: String, arg1: Any, arg2: Any): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, arg1, arg2)
    }

    actual override fun localise(msg: String, vararg args: Any): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, *args)
    }

    /** Avoid spreading, or so I'd like */
    actual override fun localiseArray(msg: String, args: Array<out Any>): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, *args)
    }

    actual override fun localiseEnglish(msg: String): String =
            localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg

    actual override fun localiseEnglish(msg: String, arg: Any): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, arg)
    }

    actual override fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, arg1, arg2)
    }

    actual override fun localiseEnglish(msg: String, vararg args: Any): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, *args)
    }

    /** Avoid spreading, or so I'd like */
    actual override fun localiseEnglishArray(msg: String, args: Array<out Any>): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str, *args)
    }

    init {
        addBundle("SpiralBase")
    }
}

actual fun localise(msg: String): String = SpiralLocale.localise(msg)
actual fun localise(msg: String, arg: Any): String = SpiralLocale.localise(msg, arg)
actual fun localise(msg: String, arg1: Any, arg2: Any): String = SpiralLocale.localise(msg, arg1, arg2)
actual fun localise(msg: String, vararg args: Any): String = SpiralLocale.localiseArray(msg, args)
actual fun localiseArray(msg: String, args: Array<out Any>): String = SpiralLocale.localiseArray(msg, args)

actual fun localiseEnglish(msg: String): String = SpiralLocale.localiseEnglish(msg)
actual fun localiseEnglish(msg: String, arg: Any): String = SpiralLocale.localiseEnglish(msg, arg)
actual fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String = SpiralLocale.localiseEnglish(msg, arg1, arg2)
actual fun localiseEnglish(msg: String, vararg args: Any): String = SpiralLocale.localiseEnglishArray(msg, args)
actual fun localiseEnglishArray(msg: String, args: Array<out Any>): String = SpiralLocale.localiseEnglishArray(msg, args)

internal actual fun defaultLocale(): CommonLocale {
    val jvmLocale = Locale.getDefault()
    return CommonLocale(jvmLocale.language, jvmLocale.country, jvmLocale.variant)
}

actual fun SpiralLocale.readConfirmation(defaultToAffirmative: Boolean): Boolean {
    val affirmative = promptAffirmative()

    val input = readLine()?.trim()?.takeIf(String::isNotBlank)
            ?: if (defaultToAffirmative) affirmative else promptNegative()

    return if (input.equals(input, true) || input.startsWith(promptShortAffirmative(), true)) AFFIRMATIVE else NEGATIVE
}