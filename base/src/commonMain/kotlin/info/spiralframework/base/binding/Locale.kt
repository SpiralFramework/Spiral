package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.AbstractSpiralLocale
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.SpiralLocale

interface SpiralLogger {
    object NoOp: SpiralLogger {
        override fun SpiralContext.error(format: String) {}
        override fun SpiralContext.error(format: String, arg: Any) {}
        override fun SpiralContext.error(format: String, th: Throwable) {}
        override fun SpiralContext.error(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.error(format: String, vararg args: Any) {}
        override fun SpiralContext.errorArray(format: String, args: Array<out Any>) {}

        override fun SpiralContext.warn(format: String) {}
        override fun SpiralContext.warn(format: String, arg: Any) {}
        override fun SpiralContext.warn(format: String, th: Throwable) {}
        override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.warn(format: String, vararg args: Any) {}
        override fun SpiralContext.warnArray(format: String, args: Array<out Any>) {}

        override fun SpiralContext.info(format: String) {}
        override fun SpiralContext.info(format: String, arg: Any) {}
        override fun SpiralContext.info(format: String, th: Throwable) {}
        override fun SpiralContext.info(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.info(format: String, vararg args: Any) {}
        override fun SpiralContext.infoArray(format: String, args: Array<out Any>) {}

        override fun SpiralContext.debug(format: String) {}
        override fun SpiralContext.debug(format: String, arg: Any) {}
        override fun SpiralContext.debug(format: String, th: Throwable) {}
        override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.debug(format: String, vararg args: Any) {}
        override fun SpiralContext.debugArray(format: String, args: Array<out Any>) {}

        override fun SpiralContext.trace(format: String) {}
        override fun SpiralContext.trace(format: String, arg: Any) {}
        override fun SpiralContext.trace(format: String, th: Throwable) {}
        override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.trace(format: String, vararg args: Any) {}
        override fun SpiralContext.traceArray(format: String, args: Array<out Any>) {}
    }
    
    fun SpiralContext.error(format: String)
    fun SpiralContext.error(format: String, arg: Any)
    fun SpiralContext.error(format: String, th: Throwable)
    fun SpiralContext.error(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.error(format: String, vararg args: Any)
    fun SpiralContext.errorArray(format: String, args: Array<out Any>)

    fun SpiralContext.warn(format: String)
    fun SpiralContext.warn(format: String, arg: Any)
    fun SpiralContext.warn(format: String, th: Throwable)
    fun SpiralContext.warn(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.warn(format: String, vararg args: Any)
    fun SpiralContext.warnArray(format: String, args: Array<out Any>)

    fun SpiralContext.info(format: String)
    fun SpiralContext.info(format: String, arg: Any)
    fun SpiralContext.info(format: String, th: Throwable)
    fun SpiralContext.info(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.info(format: String, vararg args: Any)
    fun SpiralContext.infoArray(format: String, args: Array<out Any>)

    fun SpiralContext.debug(format: String)
    fun SpiralContext.debug(format: String, arg: Any)
    fun SpiralContext.debug(format: String, th: Throwable)
    fun SpiralContext.debug(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.debug(format: String, vararg args: Any)
    fun SpiralContext.debugArray(format: String, args: Array<out Any>)

    fun SpiralContext.trace(format: String)
    fun SpiralContext.trace(format: String, arg: Any)
    fun SpiralContext.trace(format: String, th: Throwable)
    fun SpiralContext.trace(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.trace(format: String, vararg args: Any)
    fun SpiralContext.traceArray(format: String, args: Array<out Any>)
}

expect class DefaultSpiralLogger(name: String): SpiralLogger {
    override fun SpiralContext.error(format: String)
    override fun SpiralContext.error(format: String, arg: Any)
    override fun SpiralContext.error(format: String, th: Throwable)
    override fun SpiralContext.error(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.error(format: String, vararg args: Any)
    override fun SpiralContext.errorArray(format: String, args: Array<out Any>)

    override fun SpiralContext.warn(format: String)
    override fun SpiralContext.warn(format: String, arg: Any)
    override fun SpiralContext.warn(format: String, th: Throwable)
    override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.warn(format: String, vararg args: Any)
    override fun SpiralContext.warnArray(format: String, args: Array<out Any>)

    override fun SpiralContext.info(format: String)
    override fun SpiralContext.info(format: String, arg: Any)
    override fun SpiralContext.info(format: String, th: Throwable)
    override fun SpiralContext.info(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.info(format: String, vararg args: Any)
    override fun SpiralContext.infoArray(format: String, args: Array<out Any>)

    override fun SpiralContext.debug(format: String)
    override fun SpiralContext.debug(format: String, arg: Any)
    override fun SpiralContext.debug(format: String, th: Throwable)
    override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.debug(format: String, vararg args: Any)
    override fun SpiralContext.debugArray(format: String, args: Array<out Any>)

    override fun SpiralContext.trace(format: String)
    override fun SpiralContext.trace(format: String, arg: Any)
    override fun SpiralContext.trace(format: String, th: Throwable)
    override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.trace(format: String, vararg args: Any)
    override fun SpiralContext.traceArray(format: String, args: Array<out Any>)
}

expect class DefaultSpiralLocale: AbstractSpiralLocale {
    override fun localise(msg: String): String
    override fun localise(msg: String, arg: Any): String
    override fun localise(msg: String, arg1: Any, arg2: Any): String
    override fun localise(msg: String, vararg args: Any): String
    override fun localiseArray(msg: String, args: Array<out Any>): String

    override fun localiseEnglish(msg: String): String
    override fun localiseEnglish(msg: String, arg: Any): String
    override fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String
    override fun localiseEnglish(msg: String, vararg args: Any): String
    override fun localiseEnglishArray(msg: String, args: Array<out Any>): String
}

//expect fun localise(msg: String): String
//expect fun localise(msg: String, arg: Any): String
//expect fun localise(msg: String, arg1: Any, arg2: Any): String
//expect fun localise(msg: String, vararg args: Any): String
//expect fun localiseArray(msg: String, args: Array<out Any>): String
//
//expect fun localiseEnglish(msg: String): String
//expect fun localiseEnglish(msg: String, arg: Any): String
//expect fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String
//expect fun localiseEnglish(msg: String, vararg args: Any): String
//expect fun localiseEnglishArray(msg: String, args: Array<out Any>): String

fun SpiralLogger.error(format: String, context: SpiralContext) = context.error(format)
fun SpiralLogger.error(format: String, arg: Any, context: SpiralContext) = context.error(format, arg)
fun SpiralLogger.error(format: String, th: Throwable, context: SpiralContext) = context.error(format, th)
fun SpiralLogger.error(format: String, arg1: Any, arg2: Any, context: SpiralContext) = context.error(format, arg1, arg2)
fun SpiralLogger.error(format: String, vararg args: Any, context: SpiralContext) = context.errorArray(format, args)
fun SpiralLogger.errorArray(format: String, args: Array<out Any>, context: SpiralContext) = context.errorArray(format, args)

fun SpiralLogger.warn(format: String, context: SpiralContext) = context.warn(format)
fun SpiralLogger.warn(format: String, arg: Any, context: SpiralContext) = context.warn(format, arg)
fun SpiralLogger.warn(format: String, th: Throwable, context: SpiralContext) = context.warn(format, th)
fun SpiralLogger.warn(format: String, arg1: Any, arg2: Any, context: SpiralContext) = context.warn(format, arg1, arg2)
fun SpiralLogger.warn(format: String, vararg args: Any, context: SpiralContext) = context.warnArray(format, args)
fun SpiralLogger.warnArray(format: String, args: Array<out Any>, context: SpiralContext) = context.warnArray(format, args)

fun SpiralLogger.info(format: String, context: SpiralContext) = context.info(format)
fun SpiralLogger.info(format: String, arg: Any, context: SpiralContext) = context.info(format, arg)
fun SpiralLogger.info(format: String, th: Throwable, context: SpiralContext) = context.info(format, th)
fun SpiralLogger.info(format: String, arg1: Any, arg2: Any, context: SpiralContext) = context.info(format, arg1, arg2)
fun SpiralLogger.info(format: String, vararg args: Any, context: SpiralContext) = context.infoArray(format, args)
fun SpiralLogger.infoArray(format: String, args: Array<out Any>, context: SpiralContext) = context.infoArray(format, args)

fun SpiralLogger.debug(format: String, context: SpiralContext) = context.debug(format)
fun SpiralLogger.debug(format: String, arg: Any, context: SpiralContext) = context.debug(format, arg)
fun SpiralLogger.debug(format: String, th: Throwable, context: SpiralContext) = context.debug(format, th)
fun SpiralLogger.debug(format: String, arg1: Any, arg2: Any, context: SpiralContext) = context.debug(format, arg1, arg2)
fun SpiralLogger.debug(format: String, vararg args: Any, context: SpiralContext) = context.debugArray(format, args)
fun SpiralLogger.debugArray(format: String, args: Array<out Any>, context: SpiralContext) = context.debugArray(format, args)

fun SpiralLogger.trace(format: String, context: SpiralContext) = context.trace(format)
fun SpiralLogger.trace(format: String, arg: Any, context: SpiralContext) = context.trace(format, arg)
fun SpiralLogger.trace(format: String, th: Throwable, context: SpiralContext) = context.trace(format, th)
fun SpiralLogger.trace(format: String, arg1: Any, arg2: Any, context: SpiralContext) = context.trace(format, arg1, arg2)
fun SpiralLogger.trace(format: String, vararg args: Any, context: SpiralContext) = context.traceArray(format, args)
fun SpiralLogger.traceArray(format: String, args: Array<out Any>, context: SpiralContext) = context.traceArray(format, args)

internal expect fun defaultLocale(): CommonLocale

expect fun SpiralLocale.readConfirmation(defaultToAffirmative: Boolean = true): Boolean