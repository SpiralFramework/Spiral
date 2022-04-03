package info.spiralframework.base.binding

import info.spiralframework.base.common.locale.AbstractSpiralLocale
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.stripJavaQuirks
import info.spiralframework.base.common.logging.CommonSpiralLogger
import info.spiralframework.base.common.logging.SpiralLogger
import kotlinx.browser.window

public actual class DefaultSpiralLogger public actual constructor(name: String) : SpiralLogger, CommonSpiralLogger(name) {
    override val errorPrintln: (String) -> Unit = { console.error(it) }
    override val warnPrintln: (String) -> Unit = { console.warn(it) }
    override val infoPrintln: (String) -> Unit = { console.info(it) }
    override val debugPrintln: (String) -> Unit = { console.log(it) }
    override val tracePrintln: (String) -> Unit = { console.log(it) }
}

/** Since we don't have a message formatter, we have to manually replace single quotes */
public actual class DefaultSpiralLocale actual constructor() : AbstractSpiralLocale() {
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