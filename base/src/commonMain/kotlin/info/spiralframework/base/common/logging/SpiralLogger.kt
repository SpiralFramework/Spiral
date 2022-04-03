package info.spiralframework.base.common.logging

import info.spiralframework.base.common.SpiralContext

public interface SpiralLogger {
    public object NoOp : SpiralLogger {
        override val isErrorEnabled: Boolean = false
        override fun SpiralContext.error(format: String) {}
        override fun SpiralContext.error(format: String, arg: Any) {}
        override fun SpiralContext.error(format: String, th: Throwable) {}
        override fun SpiralContext.error(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.error(format: String, vararg args: Any) {}
        override fun SpiralContext.errorArray(format: String, args: Array<out Any>) {}

        override val isWarnEnabled: Boolean = false
        override fun SpiralContext.warn(format: String) {}
        override fun SpiralContext.warn(format: String, arg: Any) {}
        override fun SpiralContext.warn(format: String, th: Throwable) {}
        override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.warn(format: String, vararg args: Any) {}
        override fun SpiralContext.warnArray(format: String, args: Array<out Any>) {}

        override val isInfoEnabled: Boolean = false
        override fun SpiralContext.info(format: String) {}
        override fun SpiralContext.info(format: String, arg: Any) {}
        override fun SpiralContext.info(format: String, th: Throwable) {}
        override fun SpiralContext.info(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.info(format: String, vararg args: Any) {}
        override fun SpiralContext.infoArray(format: String, args: Array<out Any>) {}

        override val isDebugEnabled: Boolean = false
        override fun SpiralContext.debug(format: String) {}
        override fun SpiralContext.debug(format: String, arg: Any) {}
        override fun SpiralContext.debug(format: String, th: Throwable) {}
        override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.debug(format: String, vararg args: Any) {}
        override fun SpiralContext.debugArray(format: String, args: Array<out Any>) {}

        override val isTraceEnabled: Boolean = false
        override fun SpiralContext.trace(format: String) {}
        override fun SpiralContext.trace(format: String, arg: Any) {}
        override fun SpiralContext.trace(format: String, th: Throwable) {}
        override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any) {}
        override fun SpiralContext.trace(format: String, vararg args: Any) {}
        override fun SpiralContext.traceArray(format: String, args: Array<out Any>) {}
    }

    public val isErrorEnabled: Boolean
    public fun SpiralContext.error(format: String)
    public fun SpiralContext.error(format: String, arg: Any)
    public fun SpiralContext.error(format: String, th: Throwable)
    public fun SpiralContext.error(format: String, arg1: Any, arg2: Any)
    public fun SpiralContext.error(format: String, vararg args: Any)
    public fun SpiralContext.errorArray(format: String, args: Array<out Any>)

    public val isWarnEnabled: Boolean
    public fun SpiralContext.warn(format: String)
    public fun SpiralContext.warn(format: String, arg: Any)
    public fun SpiralContext.warn(format: String, th: Throwable)
    public fun SpiralContext.warn(format: String, arg1: Any, arg2: Any)
    public fun SpiralContext.warn(format: String, vararg args: Any)
    public fun SpiralContext.warnArray(format: String, args: Array<out Any>)

    public val isInfoEnabled: Boolean
    public fun SpiralContext.info(format: String)
    public fun SpiralContext.info(format: String, arg: Any)
    public fun SpiralContext.info(format: String, th: Throwable)
    public fun SpiralContext.info(format: String, arg1: Any, arg2: Any)
    public fun SpiralContext.info(format: String, vararg args: Any)
    public fun SpiralContext.infoArray(format: String, args: Array<out Any>)

    public val isDebugEnabled: Boolean
    public fun SpiralContext.debug(format: String)
    public fun SpiralContext.debug(format: String, arg: Any)
    public fun SpiralContext.debug(format: String, th: Throwable)
    public fun SpiralContext.debug(format: String, arg1: Any, arg2: Any)
    public fun SpiralContext.debug(format: String, vararg args: Any)
    public fun SpiralContext.debugArray(format: String, args: Array<out Any>)

    public val isTraceEnabled: Boolean
    public fun SpiralContext.trace(format: String)
    public fun SpiralContext.trace(format: String, arg: Any)
    public fun SpiralContext.trace(format: String, th: Throwable)
    public fun SpiralContext.trace(format: String, arg1: Any, arg2: Any)
    public fun SpiralContext.trace(format: String, vararg args: Any)
    public fun SpiralContext.traceArray(format: String, args: Array<out Any>)
}

public inline fun <T> T.error(block: T.() -> Unit): Unit where T : SpiralLogger, T : SpiralContext = if (isErrorEnabled) block() else Unit
public fun <T> T.error(format: String): Unit where T : SpiralLogger, T : SpiralContext = error(format)
public fun <T> T.error(format: String, arg: Any): Unit where T : SpiralLogger, T : SpiralContext = error(format, arg)
public fun <T> T.error(format: String, th: Throwable): Unit where T : SpiralLogger, T : SpiralContext = error(format, th)
public fun <T> T.error(format: String, arg1: Any, arg2: Any): Unit where T : SpiralLogger, T : SpiralContext = error(format, arg1, arg2)
public fun <T> T.error(format: String, vararg args: Any): Unit where T : SpiralLogger, T : SpiralContext = errorArray(format, args)
public fun <T> T.errorArray(format: String, args: Array<out Any>): Unit where T : SpiralLogger, T : SpiralContext = errorArray(format, args)

public inline fun <T> T.warn(block: T.() -> Unit): Unit where T : SpiralLogger, T : SpiralContext = if (isWarnEnabled) block() else Unit
public fun <T> T.warn(format: String): Unit where T : SpiralLogger, T : SpiralContext = warn(format)
public fun <T> T.warn(format: String, arg: Any): Unit where T : SpiralLogger, T : SpiralContext = warn(format, arg)
public fun <T> T.warn(format: String, th: Throwable): Unit where T : SpiralLogger, T : SpiralContext = warn(format, th)
public fun <T> T.warn(format: String, arg1: Any, arg2: Any): Unit where T : SpiralLogger, T : SpiralContext = warn(format, arg1, arg2)
public fun <T> T.warn(format: String, vararg args: Any): Unit where T : SpiralLogger, T : SpiralContext = warnArray(format, args)
public fun <T> T.warnArray(format: String, args: Array<out Any>): Unit where T : SpiralLogger, T : SpiralContext = warnArray(format, args)

public inline fun <T> T.info(block: T.() -> Unit): Unit where T : SpiralLogger, T : SpiralContext = if (isInfoEnabled) block() else Unit
public fun <T> T.info(format: String): Unit where T : SpiralLogger, T : SpiralContext = info(format)
public fun <T> T.info(format: String, arg: Any): Unit where T : SpiralLogger, T : SpiralContext = info(format, arg)
public fun <T> T.info(format: String, th: Throwable): Unit where T : SpiralLogger, T : SpiralContext = info(format, th)
public fun <T> T.info(format: String, arg1: Any, arg2: Any): Unit where T : SpiralLogger, T : SpiralContext = info(format, arg1, arg2)
public fun <T> T.info(format: String, vararg args: Any): Unit where T : SpiralLogger, T : SpiralContext = infoArray(format, args)
public fun <T> T.infoArray(format: String, args: Array<out Any>): Unit where T : SpiralLogger, T : SpiralContext = infoArray(format, args)

public inline fun <T> T.debug(block: T.() -> Unit): Unit where T : SpiralLogger, T : SpiralContext = if (isDebugEnabled) block() else Unit
public fun <T> T.debug(format: String): Unit where T : SpiralLogger, T : SpiralContext = debug(format)
public fun <T> T.debug(format: String, arg: Any): Unit where T : SpiralLogger, T : SpiralContext = debug(format, arg)
public fun <T> T.debug(format: String, th: Throwable): Unit where T : SpiralLogger, T : SpiralContext = debug(format, th)
public fun <T> T.debug(format: String, arg1: Any, arg2: Any): Unit where T : SpiralLogger, T : SpiralContext = debug(format, arg1, arg2)
public fun <T> T.debug(format: String, vararg args: Any): Unit where T : SpiralLogger, T : SpiralContext = debugArray(format, args)
public fun <T> T.debugArray(format: String, args: Array<out Any>): Unit where T : SpiralLogger, T : SpiralContext = debugArray(format, args)

public inline fun <T> T.trace(block: T.() -> Unit): Unit where T : SpiralLogger, T : SpiralContext = if (isTraceEnabled) block() else Unit
public fun <T> T.trace(format: String): Unit where T : SpiralLogger, T : SpiralContext = trace(format)
public fun <T> T.trace(format: String, arg: Any): Unit where T : SpiralLogger, T : SpiralContext = trace(format, arg)
public fun <T> T.trace(format: String, th: Throwable): Unit where T : SpiralLogger, T : SpiralContext = trace(format, th)
public fun <T> T.trace(format: String, arg1: Any, arg2: Any): Unit where T : SpiralLogger, T : SpiralContext = trace(format, arg1, arg2)
public fun <T> T.trace(format: String, vararg args: Any): Unit where T : SpiralLogger, T : SpiralContext = traceArray(format, args)
public fun <T> T.traceArray(format: String, args: Array<out Any>): Unit where T : SpiralLogger, T : SpiralContext = traceArray(format, args)