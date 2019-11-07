package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.AbstractSpiralLocale
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.stripJavaQuirks
import info.spiralframework.base.common.logging.SpiralLogger
import kotlin.browser.window

@ExperimentalUnsignedTypes
actual class DefaultSpiralLogger actual constructor(val name: String) : SpiralLogger {
    actual override fun SpiralContext.error(format: String) = console.error(formatText("ERROR", format))
    actual override fun SpiralContext.error(format: String, arg: Any) = console.error(formatText("ERROR", format, arg))
    actual override fun SpiralContext.error(format: String, th: Throwable) = console.error(formatText("ERROR", format, th))
    actual override fun SpiralContext.error(format: String, arg1: Any, arg2: Any) = console.error(formatText("ERROR", format, arg1, arg2))
    actual override fun SpiralContext.error(format: String, vararg args: Any) = console.error(formatArrayText("ERROR", format, args))
    actual override fun SpiralContext.errorArray(format: String, args: Array<out Any>) = console.error(formatArrayText("ERROR", format, args))

    actual override fun SpiralContext.warn(format: String) = console.warn(formatText("WARN", format))
    actual override fun SpiralContext.warn(format: String, arg: Any) = console.warn(formatText("WARN", format, arg))
    actual override fun SpiralContext.warn(format: String, th: Throwable) = console.warn(formatText("WARN", format, th))
    actual override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any) = console.warn(formatText("WARN", format, arg1, arg2))
    actual override fun SpiralContext.warn(format: String, vararg args: Any) = console.warn(formatArrayText("WARN", format, args))
    actual override fun SpiralContext.warnArray(format: String, args: Array<out Any>) = console.warn(formatArrayText("WARN", format, args))

    actual override fun SpiralContext.info(format: String) = console.info(formatText("INFO", format))
    actual override fun SpiralContext.info(format: String, arg: Any) = console.info(formatText("INFO", format, arg))
    actual override fun SpiralContext.info(format: String, th: Throwable) = console.info(formatText("INFO", format, th))
    actual override fun SpiralContext.info(format: String, arg1: Any, arg2: Any) = console.info(formatText("INFO", format, arg1, arg2))
    actual override fun SpiralContext.info(format: String, vararg args: Any) = console.info(formatArrayText("INFO", format, args))
    actual override fun SpiralContext.infoArray(format: String, args: Array<out Any>) = console.info(formatArrayText("INFO", format, args))

    actual override fun SpiralContext.debug(format: String) = console.log(formatText("DEBUG", format))
    actual override fun SpiralContext.debug(format: String, arg: Any) = console.log(formatText("DEBUG", format, arg))
    actual override fun SpiralContext.debug(format: String, th: Throwable) = console.log(formatText("DEBUG", format, th))
    actual override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any) = console.log(formatText("DEBUG", format, arg1, arg2))
    actual override fun SpiralContext.debug(format: String, vararg args: Any) = console.log(formatArrayText("DEBUG", format, args))
    actual override fun SpiralContext.debugArray(format: String, args: Array<out Any>) = console.log(formatArrayText("DEBUG", format, args))

    actual override fun SpiralContext.trace(format: String) = console.log(formatText("TRACE", format))
    actual override fun SpiralContext.trace(format: String, arg: Any) = console.log(formatText("TRACE", format, arg))
    actual override fun SpiralContext.trace(format: String, th: Throwable) = console.log(formatText("TRACE", format, th))
    actual override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any) = console.log(formatText("TRACE", format, arg1, arg2))
    actual override fun SpiralContext.trace(format: String, vararg args: Any) = console.log(formatArrayText("TRACE", format, args))
    actual override fun SpiralContext.traceArray(format: String, args: Array<out Any>) = console.log(formatArrayText("TRACE", format, args))

    fun SpiralContext.formatText(level: String, format: String) = formatTextWithName(level, localise(format))
    fun SpiralContext.formatText(level: String, format: String, arg: Any) = formatTextWithName(level, localise(format, arg))
    fun SpiralContext.formatText(level: String, format: String, th: Throwable) = buildString {
        append('[')
        append(name)
        append(" / ")
        append(level)
        append("] ")
        append(localise(format, th))
        append(" - ")
        append(th.message)
    }

    fun SpiralContext.formatText(level: String, format: String, arg1: Any, arg2: Any) = formatTextWithName(level, localise(format, arg1, arg2))
    fun SpiralContext.formatText(level: String, format: String, vararg args: Any) = formatTextWithName(level, localiseArray(format, args))
    fun SpiralContext.formatArrayText(level: String, format: String, args: Array<out Any>) = formatTextWithName(level, localiseArray(format, args))

    fun formatTextWithName(level: String, msg: String): String = buildString {
        append('[')
        append(name)
        append(" / ")
        append(level)
        append("] ")
        append(msg)
    }
}

/** Since we don't have a message formatter, we have to manually replace single quotes */
actual class DefaultSpiralLocale actual constructor() : AbstractSpiralLocale() {
    actual override fun localise(msg: String): String =
            stripJavaQuirks(localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)

    actual override fun localise(msg: String, arg: Any): String {
        val str = stripJavaQuirks(localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)
        return str.replace("{0}", arg.toString())
    }

    actual override fun localise(msg: String, arg1: Any, arg2: Any): String {
        val str = stripJavaQuirks(localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)
        return str.replace("{0}", arg1.toString()).replace("{1}", arg2.toString())
    }

    actual override fun localise(msg: String, vararg args: Any): String = localiseArray(msg, args)
    actual override fun localiseArray(msg: String, args: Array<out Any>): String {
        var str = stripJavaQuirks(localisationBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)
        for (i in args.indices) {
            str = str.replace("{$i}", args[i].toString())
        }
        return str
    }

    actual override fun localiseEnglish(msg: String): String =
            stripJavaQuirks(englishBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)

    actual override fun localiseEnglish(msg: String, arg: Any): String {
        val str = stripJavaQuirks(englishBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)
        return str.replace("{0}", arg.toString())
    }

    actual override fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String {
        val str = stripJavaQuirks(englishBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)
        return str.replace("{0}", arg1.toString()).replace("{1}", arg2.toString())
    }

    actual override fun localiseEnglish(msg: String, vararg args: Any): String = localiseEnglishArray(msg, args)
    actual override fun localiseEnglishArray(msg: String, args: Array<out Any>): String {
        var str = stripJavaQuirks(englishBundles.firstOrNull { bundle -> bundle.containsKey(msg) }?.get(msg) ?: msg)
        for (i in args.indices) {
            str = str.replace("{$i}", args[i].toString())
        }
        return str
    }
}

internal actual fun defaultLocale(): CommonLocale {
    //TODO: Check if this might break
    val preferredLocales = window.navigator.languages
    val preferredLocaleComponents = preferredLocales.firstOrNull()?.split('-') ?: return CommonLocale.ENGLISH
    return CommonLocale(
            preferredLocaleComponents.getOrNull(0) ?: "",
            preferredLocaleComponents.getOrNull(1) ?: "",
            preferredLocaleComponents.getOrNull(2) ?: ""
    )
}