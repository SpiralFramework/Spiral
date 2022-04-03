package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.AbstractSpiralLocale
import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.logging.SpiralLogger

public expect class DefaultSpiralLogger(name: String): SpiralLogger {
    override val isErrorEnabled: Boolean
    override fun SpiralContext.error(format: String)
    override fun SpiralContext.error(format: String, arg: Any)
    override fun SpiralContext.error(format: String, th: Throwable)
    override fun SpiralContext.error(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.error(format: String, vararg args: Any)
    override fun SpiralContext.errorArray(format: String, args: Array<out Any>)

    override val isWarnEnabled: Boolean
    override fun SpiralContext.warn(format: String)
    override fun SpiralContext.warn(format: String, arg: Any)
    override fun SpiralContext.warn(format: String, th: Throwable)
    override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.warn(format: String, vararg args: Any)
    override fun SpiralContext.warnArray(format: String, args: Array<out Any>)

    override val isInfoEnabled: Boolean
    override fun SpiralContext.info(format: String)
    override fun SpiralContext.info(format: String, arg: Any)
    override fun SpiralContext.info(format: String, th: Throwable)
    override fun SpiralContext.info(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.info(format: String, vararg args: Any)
    override fun SpiralContext.infoArray(format: String, args: Array<out Any>)

    override val isDebugEnabled: Boolean
    override fun SpiralContext.debug(format: String)
    override fun SpiralContext.debug(format: String, arg: Any)
    override fun SpiralContext.debug(format: String, th: Throwable)
    override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.debug(format: String, vararg args: Any)
    override fun SpiralContext.debugArray(format: String, args: Array<out Any>)

    override val isTraceEnabled: Boolean
    override fun SpiralContext.trace(format: String)
    override fun SpiralContext.trace(format: String, arg: Any)
    override fun SpiralContext.trace(format: String, th: Throwable)
    override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any)
    override fun SpiralContext.trace(format: String, vararg args: Any)
    override fun SpiralContext.traceArray(format: String, args: Array<out Any>)
}

public expect class DefaultSpiralLocale(): AbstractSpiralLocale {
    override fun localise(msg: String): String
    override fun localise(msg: String, arg: Any): String
    override fun localise(msg: String, arg1: Any, arg2: Any): String
    override fun localise(msg: String, vararg args: Any): String
    override fun localiseArray(msg: String, args: Array<out Any>): String
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

internal expect fun defaultLocale(): CommonLocale

//TODO: Don't think this needs to be in
//expect fun SpiralLocale.readConfirmation(defaultToAffirmative: Boolean = true): Boolean