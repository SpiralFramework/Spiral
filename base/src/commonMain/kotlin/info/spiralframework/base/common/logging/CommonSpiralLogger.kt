package info.spiralframework.base.common.logging

import info.spiralframework.base.common.SpiralContext

class CommonSpiralLogger(val name: String, var loggerLevel: Int = ERROR) : SpiralLogger {
    companion object {
        const val TRACE = 0b0000001
        const val DEBUG = 0b0000010
        const val INFO = 0b0000100
        const val WARN = 0b0001000
        const val ERROR = 0b0010000

        const val TRACE_LABEL = "TRACE"
        const val DEBUG_LABEL = "DEBUG"
        const val INFO_LABEL = " INFO"
        const val WARN_LABEL = " WARN"
        const val ERROR_LABEL = "ERROR"
    }

    constructor(name: String, errorEnabled: Boolean = false, warnEnabled: Boolean = false, infoEnabled: Boolean = false, debugEnabled: Boolean = false, traceEnabled: Boolean = false)
            : this(name, (if (errorEnabled) ERROR else 0) or (if (warnEnabled) WARN else 0) or (if (infoEnabled) INFO else 0) or (if (debugEnabled) DEBUG else 0) or (if (traceEnabled) TRACE else 0))

    var isTraceEnabled
        get() = isLevelEnabled(TRACE)
        set(value) = setLevelEnabled(TRACE, value)

    var isDebugEnabled
        get() = isLevelEnabled(DEBUG)
        set(value) = setLevelEnabled(DEBUG, value)

    var isInfoEnabled
        get() = isLevelEnabled(INFO)
        set(value) = setLevelEnabled(INFO, value)

    var isWarnEnabled
        get() = isLevelEnabled(WARN)
        set(value) = setLevelEnabled(WARN, value)

    var isErrorEnabled
        get() = isLevelEnabled(ERROR)
        set(value) = setLevelEnabled(ERROR, value)

    fun isLevelEnabled(level: Int): Boolean = loggerLevel and level == level
    fun setLevelEnabled(level: Int, enabled: Boolean) {
        if (enabled) {
            loggerLevel = loggerLevel or level
        } else {
            loggerLevel = loggerLevel and level.inv()
        }
    }

    override fun SpiralContext.error(format: String) = if (isErrorEnabled) println(formatText(ERROR_LABEL, format)) else Unit
    override fun SpiralContext.error(format: String, arg: Any) = if (isErrorEnabled) println(formatText(ERROR_LABEL, format, arg)) else Unit
    override fun SpiralContext.error(format: String, th: Throwable) = if (isErrorEnabled) println(formatText(ERROR_LABEL, format, th)) else Unit
    override fun SpiralContext.error(format: String, arg1: Any, arg2: Any) = if (isErrorEnabled) println(formatText(ERROR_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.error(format: String, vararg args: Any) = if (isErrorEnabled) println(formatArrayText(ERROR_LABEL, format, args)) else Unit
    override fun SpiralContext.errorArray(format: String, args: Array<out Any>) = if (isErrorEnabled) println(formatArrayText(ERROR_LABEL, format, args)) else Unit

    override fun SpiralContext.warn(format: String) = if (isWarnEnabled) println(formatText(WARN_LABEL, format)) else Unit
    override fun SpiralContext.warn(format: String, arg: Any) = if (isWarnEnabled) println(formatText(WARN_LABEL, format, arg)) else Unit
    override fun SpiralContext.warn(format: String, th: Throwable) = if (isWarnEnabled) println(formatText(WARN_LABEL, format, th)) else Unit
    override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any) = if (isWarnEnabled) println(formatText(WARN_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.warn(format: String, vararg args: Any) = if (isWarnEnabled) println(formatArrayText(WARN_LABEL, format, args)) else Unit
    override fun SpiralContext.warnArray(format: String, args: Array<out Any>) = if (isWarnEnabled) println(formatArrayText(WARN_LABEL, format, args)) else Unit

    override fun SpiralContext.info(format: String) = if (isInfoEnabled) println(formatText(INFO_LABEL, format)) else Unit
    override fun SpiralContext.info(format: String, arg: Any) = if (isInfoEnabled) println(formatText(INFO_LABEL, format, arg)) else Unit
    override fun SpiralContext.info(format: String, th: Throwable) = if (isInfoEnabled) println(formatText(INFO_LABEL, format, th)) else Unit
    override fun SpiralContext.info(format: String, arg1: Any, arg2: Any) = if (isInfoEnabled) println(formatText(INFO_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.info(format: String, vararg args: Any) = if (isInfoEnabled) println(formatArrayText(INFO_LABEL, format, args)) else Unit
    override fun SpiralContext.infoArray(format: String, args: Array<out Any>) = if (isInfoEnabled) println(formatArrayText(INFO_LABEL, format, args)) else Unit

    override fun SpiralContext.debug(format: String) = if (isDebugEnabled) println(formatText(DEBUG_LABEL, format)) else Unit
    override fun SpiralContext.debug(format: String, arg: Any) = if (isDebugEnabled) println(formatText(DEBUG_LABEL, format, arg)) else Unit
    override fun SpiralContext.debug(format: String, th: Throwable) = if (isDebugEnabled) println(formatText(DEBUG_LABEL, format, th)) else Unit
    override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any) = if (isDebugEnabled) println(formatText(DEBUG_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.debug(format: String, vararg args: Any) = if (isDebugEnabled) println(formatArrayText(DEBUG_LABEL, format, args)) else Unit
    override fun SpiralContext.debugArray(format: String, args: Array<out Any>) = if (isDebugEnabled) println(formatArrayText(DEBUG_LABEL, format, args)) else Unit

    override fun SpiralContext.trace(format: String) = if (isTraceEnabled) println(formatText(TRACE_LABEL, format)) else Unit
    override fun SpiralContext.trace(format: String, arg: Any) = if (isTraceEnabled) println(formatText(TRACE_LABEL, format, arg)) else Unit
    override fun SpiralContext.trace(format: String, th: Throwable) = if (isTraceEnabled) println(formatText(TRACE_LABEL, format, th)) else Unit
    override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any) = if (isTraceEnabled) println(formatText(TRACE_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.trace(format: String, vararg args: Any) = if (isTraceEnabled) println(formatArrayText(TRACE_LABEL, format, args)) else Unit
    override fun SpiralContext.traceArray(format: String, args: Array<out Any>) = if (isTraceEnabled) println(formatArrayText(TRACE_LABEL, format, args)) else Unit

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