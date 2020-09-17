package info.spiralframework.base.common.logging

import info.spiralframework.base.common.SpiralContext

interface SpiralLogger {
    object NoOp : SpiralLogger {
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

    val isErrorEnabled: Boolean
    fun SpiralContext.error(format: String)
    fun SpiralContext.error(format: String, arg: Any)
    fun SpiralContext.error(format: String, th: Throwable)
    fun SpiralContext.error(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.error(format: String, vararg args: Any)
    fun SpiralContext.errorArray(format: String, args: Array<out Any>)

    val isWarnEnabled: Boolean
    fun SpiralContext.warn(format: String)
    fun SpiralContext.warn(format: String, arg: Any)
    fun SpiralContext.warn(format: String, th: Throwable)
    fun SpiralContext.warn(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.warn(format: String, vararg args: Any)
    fun SpiralContext.warnArray(format: String, args: Array<out Any>)

    val isInfoEnabled: Boolean
    fun SpiralContext.info(format: String)
    fun SpiralContext.info(format: String, arg: Any)
    fun SpiralContext.info(format: String, th: Throwable)
    fun SpiralContext.info(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.info(format: String, vararg args: Any)
    fun SpiralContext.infoArray(format: String, args: Array<out Any>)

    val isDebugEnabled: Boolean
    fun SpiralContext.debug(format: String)
    fun SpiralContext.debug(format: String, arg: Any)
    fun SpiralContext.debug(format: String, th: Throwable)
    fun SpiralContext.debug(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.debug(format: String, vararg args: Any)
    fun SpiralContext.debugArray(format: String, args: Array<out Any>)

    val isTraceEnabled: Boolean
    fun SpiralContext.trace(format: String)
    fun SpiralContext.trace(format: String, arg: Any)
    fun SpiralContext.trace(format: String, th: Throwable)
    fun SpiralContext.trace(format: String, arg1: Any, arg2: Any)
    fun SpiralContext.trace(format: String, vararg args: Any)
    fun SpiralContext.traceArray(format: String, args: Array<out Any>)
}

inline fun <T> T.error(block: T.() -> Unit) where T : SpiralLogger, T : SpiralContext = if (isErrorEnabled) block() else Unit
inline fun <T> T.error(format: String) where T : SpiralLogger, T : SpiralContext = error(format)
inline fun <T> T.error(format: String, arg: Any) where T : SpiralLogger, T : SpiralContext = error(format, arg)
inline fun <T> T.error(format: String, th: Throwable) where T : SpiralLogger, T : SpiralContext = error(format, th)
inline fun <T> T.error(format: String, arg1: Any, arg2: Any) where T : SpiralLogger, T : SpiralContext = error(format, arg1, arg2)
inline fun <T> T.error(format: String, vararg args: Any) where T : SpiralLogger, T : SpiralContext = errorArray(format, args)
inline fun <T> T.errorArray(format: String, args: Array<out Any>) where T : SpiralLogger, T : SpiralContext = errorArray(format, args)

inline fun <T> T.warn(block: T.() -> Unit) where T : SpiralLogger, T : SpiralContext = if (isWarnEnabled) block() else Unit
inline fun <T> T.warn(format: String) where T : SpiralLogger, T : SpiralContext = warn(format)
inline fun <T> T.warn(format: String, arg: Any) where T : SpiralLogger, T : SpiralContext = warn(format, arg)
inline fun <T> T.warn(format: String, th: Throwable) where T : SpiralLogger, T : SpiralContext = warn(format, th)
inline fun <T> T.warn(format: String, arg1: Any, arg2: Any) where T : SpiralLogger, T : SpiralContext = warn(format, arg1, arg2)
inline fun <T> T.warn(format: String, vararg args: Any) where T : SpiralLogger, T : SpiralContext = warnArray(format, args)
inline fun <T> T.warnArray(format: String, args: Array<out Any>) where T : SpiralLogger, T : SpiralContext = warnArray(format, args)

inline fun <T> T.info(block: T.() -> Unit) where T : SpiralLogger, T : SpiralContext = if (isInfoEnabled) block() else Unit
inline fun <T> T.info(format: String) where T : SpiralLogger, T : SpiralContext = info(format)
inline fun <T> T.info(format: String, arg: Any) where T : SpiralLogger, T : SpiralContext = info(format, arg)
inline fun <T> T.info(format: String, th: Throwable) where T : SpiralLogger, T : SpiralContext = info(format, th)
inline fun <T> T.info(format: String, arg1: Any, arg2: Any) where T : SpiralLogger, T : SpiralContext = info(format, arg1, arg2)
inline fun <T> T.info(format: String, vararg args: Any) where T : SpiralLogger, T : SpiralContext = infoArray(format, args)
inline fun <T> T.infoArray(format: String, args: Array<out Any>) where T : SpiralLogger, T : SpiralContext = infoArray(format, args)

inline fun <T> T.debug(block: T.() -> Unit) where T : SpiralLogger, T : SpiralContext = if (isDebugEnabled) block() else Unit
inline fun <T> T.debug(format: String) where T : SpiralLogger, T : SpiralContext = debug(format)
inline fun <T> T.debug(format: String, arg: Any) where T : SpiralLogger, T : SpiralContext = debug(format, arg)
inline fun <T> T.debug(format: String, th: Throwable) where T : SpiralLogger, T : SpiralContext = debug(format, th)
inline fun <T> T.debug(format: String, arg1: Any, arg2: Any) where T : SpiralLogger, T : SpiralContext = debug(format, arg1, arg2)
inline fun <T> T.debug(format: String, vararg args: Any) where T : SpiralLogger, T : SpiralContext = debugArray(format, args)
inline fun <T> T.debugArray(format: String, args: Array<out Any>) where T : SpiralLogger, T : SpiralContext = debugArray(format, args)

inline fun <T> T.trace(block: T.() -> Unit) where T : SpiralLogger, T : SpiralContext = if (isTraceEnabled) block() else Unit
inline fun <T> T.trace(format: String) where T : SpiralLogger, T : SpiralContext = trace(format)
inline fun <T> T.trace(format: String, arg: Any) where T : SpiralLogger, T : SpiralContext = trace(format, arg)
inline fun <T> T.trace(format: String, th: Throwable) where T : SpiralLogger, T : SpiralContext = trace(format, th)
inline fun <T> T.trace(format: String, arg1: Any, arg2: Any) where T : SpiralLogger, T : SpiralContext = trace(format, arg1, arg2)
inline fun <T> T.trace(format: String, vararg args: Any) where T : SpiralLogger, T : SpiralContext = traceArray(format, args)
inline fun <T> T.traceArray(format: String, args: Array<out Any>) where T : SpiralLogger, T : SpiralContext = traceArray(format, args)