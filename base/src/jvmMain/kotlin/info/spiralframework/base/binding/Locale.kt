package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.*
import info.spiralframework.base.common.logging.SpiralLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.MessageFormat
import java.util.*

public actual class DefaultSpiralLogger(private val logger: Logger) : SpiralLogger {
    public actual constructor(name: String) : this(LoggerFactory.getLogger(name))

    actual override val isErrorEnabled: Boolean
        get() = logger.isErrorEnabled

    actual override val isWarnEnabled: Boolean
        get() = logger.isWarnEnabled

    actual override val isInfoEnabled: Boolean
        get() = logger.isInfoEnabled

    actual override val isDebugEnabled: Boolean
        get() = logger.isDebugEnabled

    actual override val isTraceEnabled: Boolean
        get() = logger.isTraceEnabled

    actual override fun SpiralContext.error(format: String): Unit = if (logger.isErrorEnabled) logger.error(localise(format)) else Unit
    actual override fun SpiralContext.error(format: String, arg: Any): Unit = if (logger.isErrorEnabled) logger.error(localise(format, arg), arg) else Unit
    actual override fun SpiralContext.error(format: String, th: Throwable): Unit = if (logger.isErrorEnabled) logger.error(localise(format), th) else Unit
    actual override fun SpiralContext.error(format: String, arg1: Any, arg2: Any): Unit = if (logger.isErrorEnabled) logger.error(localise(format, arg1, arg2), arg2) else Unit
    actual override fun SpiralContext.error(format: String, vararg args: Any): Unit = if (logger.isErrorEnabled) logger.error(localiseArray(format, args), args.lastOrNull()) else Unit
    actual override fun SpiralContext.errorArray(format: String, args: Array<out Any>): Unit = if (logger.isErrorEnabled) logger.error(localiseArray(format, args), args.lastOrNull()) else Unit

    actual override fun SpiralContext.warn(format: String): Unit = if (logger.isWarnEnabled) logger.warn(localise(format)) else Unit
    actual override fun SpiralContext.warn(format: String, arg: Any): Unit = if (logger.isWarnEnabled) logger.warn(localise(format, arg), arg) else Unit
    actual override fun SpiralContext.warn(format: String, th: Throwable): Unit = if (logger.isWarnEnabled) logger.warn(localise(format), th) else Unit
    actual override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any): Unit = if (logger.isWarnEnabled) logger.warn(localise(format, arg1, arg2), arg2) else Unit
    actual override fun SpiralContext.warn(format: String, vararg args: Any): Unit = if (logger.isWarnEnabled) logger.warn(localiseArray(format, args), args.lastOrNull()) else Unit
    actual override fun SpiralContext.warnArray(format: String, args: Array<out Any>): Unit = if (logger.isWarnEnabled) logger.warn(localiseArray(format, args), args.lastOrNull()) else Unit

    actual override fun SpiralContext.info(format: String): Unit = if (logger.isInfoEnabled) logger.info(localise(format)) else Unit
    actual override fun SpiralContext.info(format: String, arg: Any): Unit = if (logger.isInfoEnabled) logger.info(localise(format, arg), arg) else Unit
    actual override fun SpiralContext.info(format: String, th: Throwable): Unit = if (logger.isInfoEnabled) logger.info(localise(format), th) else Unit
    actual override fun SpiralContext.info(format: String, arg1: Any, arg2: Any): Unit = if (logger.isInfoEnabled) logger.info(localise(format, arg1, arg2), arg2) else Unit
    actual override fun SpiralContext.info(format: String, vararg args: Any): Unit = if (logger.isInfoEnabled) logger.info(localiseArray(format, args), args.lastOrNull()) else Unit
    actual override fun SpiralContext.infoArray(format: String, args: Array<out Any>): Unit = if (logger.isInfoEnabled) logger.info(localiseArray(format, args), args.lastOrNull()) else Unit

    actual override fun SpiralContext.debug(format: String): Unit = if (logger.isDebugEnabled) logger.debug(localise(format)) else Unit
    actual override fun SpiralContext.debug(format: String, arg: Any): Unit = if (logger.isDebugEnabled) logger.debug(localise(format, arg), arg) else Unit
    actual override fun SpiralContext.debug(format: String, th: Throwable): Unit = if (logger.isDebugEnabled) logger.debug(localise(format), th) else Unit
    actual override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any): Unit = if (logger.isDebugEnabled) logger.debug(localise(format, arg1, arg2), arg2) else Unit
    actual override fun SpiralContext.debug(format: String, vararg args: Any): Unit = if (logger.isDebugEnabled) logger.debug(localiseArray(format, args), args.lastOrNull()) else Unit
    actual override fun SpiralContext.debugArray(format: String, args: Array<out Any>): Unit = if (logger.isDebugEnabled) logger.debug(localiseArray(format, args), args.lastOrNull()) else Unit

    actual override fun SpiralContext.trace(format: String): Unit = if (logger.isTraceEnabled) logger.trace(localise(format)) else Unit
    actual override fun SpiralContext.trace(format: String, arg: Any): Unit = if (logger.isTraceEnabled) logger.trace(localise(format, arg), arg) else Unit
    actual override fun SpiralContext.trace(format: String, th: Throwable): Unit = if (logger.isTraceEnabled) logger.trace(localise(format), th) else Unit
    actual override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any): Unit = if (logger.isTraceEnabled) logger.trace(localise(format, arg1, arg2), arg2) else Unit
    actual override fun SpiralContext.trace(format: String, vararg args: Any): Unit = if (logger.isTraceEnabled) logger.trace(localiseArray(format, args), args.lastOrNull()) else Unit
    actual override fun SpiralContext.traceArray(format: String, args: Array<out Any>): Unit = if (logger.isTraceEnabled) logger.trace(localiseArray(format, args), args.lastOrNull()) else Unit
}

public actual class DefaultSpiralLocale : AbstractSpiralLocale() {
    actual override fun localise(msg: String): String {
        val str = localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg
        return MessageFormat.format(str)
    }

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
}

//actual fun localise(msg: String): String = SpiralLocale.localise(msg)
//actual fun localise(msg: String, arg: Any): String = SpiralLocale.localise(msg, arg)
//actual fun localise(msg: String, arg1: Any, arg2: Any): String = SpiralLocale.localise(msg, arg1, arg2)
//actual fun localise(msg: String, vararg args: Any): String = SpiralLocale.localiseArray(msg, args)
//actual fun localiseArray(msg: String, args: Array<out Any>): String = SpiralLocale.localiseArray(msg, args)
//
//actual fun localiseEnglish(msg: String): String = SpiralLocale.localiseEnglish(msg)
//actual fun localiseEnglish(msg: String, arg: Any): String = SpiralLocale.localiseEnglish(msg, arg)
//actual fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String = SpiralLocale.localiseEnglish(msg, arg1, arg2)
//actual fun localiseEnglish(msg: String, vararg args: Any): String = SpiralLocale.localiseEnglishArray(msg, args)
//actual fun localiseEnglishArray(msg: String, args: Array<out Any>): String = SpiralLocale.localiseEnglishArray(msg, args)

internal actual fun defaultLocale(): CommonLocale {
    val jvmLocale = Locale.getDefault()
    return CommonLocale(jvmLocale.language, jvmLocale.country, jvmLocale.variant)
}

@Suppress("NOTHING_TO_INLINE")
public inline fun SpiralLocale.prompt(promptText: String, defaultToAffirmative: Boolean = true): Boolean {
    print(localise(promptText))
    return readConfirmation(defaultToAffirmative)
}

public fun SpiralLocale.readConfirmation(defaultToAffirmative: Boolean = true): Boolean {
    val affirmative = promptAffirmative()

    val input = readLine()?.trim()?.takeIf(String::isNotBlank)
            ?: if (defaultToAffirmative) affirmative else promptNegative()

    return if (input.equals(input, true) || input.startsWith(promptShortAffirmative(), true)) AFFIRMATIVE else NEGATIVE
}