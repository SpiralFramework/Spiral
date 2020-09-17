package info.spiralframework.base.common.logging

import info.spiralframework.base.common.SpiralContext

open class CommonSpiralLogger(val name: String, var loggerLevel: Int = ERROR) : SpiralLogger {
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

    override val isTraceEnabled
        get() = isLevelEnabled(TRACE)

    override val isDebugEnabled
        get() = isLevelEnabled(DEBUG)

    override val isInfoEnabled
        get() = isLevelEnabled(INFO)

    override val isWarnEnabled
        get() = isLevelEnabled(WARN)

    override val isErrorEnabled
        get() = isLevelEnabled(ERROR)

    var traceEnabled
        get() = isLevelEnabled(TRACE)
        set(value) = setLevelEnabled(TRACE, value)

    var debugEnabled
        get() = isLevelEnabled(DEBUG)
        set(value) = setLevelEnabled(DEBUG, value)

    var infoEnabled
        get() = isLevelEnabled(INFO)
        set(value) = setLevelEnabled(INFO, value)

    var warnEnabled
        get() = isLevelEnabled(WARN)
        set(value) = setLevelEnabled(WARN, value)

    var errorEnabled
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

    protected open val errorPrintln: (String) -> Unit = ::println
    protected open val warnPrintln: (String) -> Unit = ::println
    protected open val infoPrintln: (String) -> Unit = ::println
    protected open val debugPrintln: (String) -> Unit = ::println
    protected open val tracePrintln: (String) -> Unit = ::println

    override fun SpiralContext.error(format: String) = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format)) else Unit
    override fun SpiralContext.error(format: String, arg: Any) = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format, arg)) else Unit
    override fun SpiralContext.error(format: String, th: Throwable) = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format, th)) else Unit
    override fun SpiralContext.error(format: String, arg1: Any, arg2: Any) = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.error(format: String, vararg args: Any) = if (isErrorEnabled) errorPrintln(formatArrayText(ERROR_LABEL, format, args)) else Unit
    override fun SpiralContext.errorArray(format: String, args: Array<out Any>) = if (isErrorEnabled) errorPrintln(formatArrayText(ERROR_LABEL, format, args)) else Unit

    override fun SpiralContext.warn(format: String) = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format)) else Unit
    override fun SpiralContext.warn(format: String, arg: Any) = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format, arg)) else Unit
    override fun SpiralContext.warn(format: String, th: Throwable) = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format, th)) else Unit
    override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any) = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.warn(format: String, vararg args: Any) = if (isWarnEnabled) warnPrintln(formatArrayText(WARN_LABEL, format, args)) else Unit
    override fun SpiralContext.warnArray(format: String, args: Array<out Any>) = if (isWarnEnabled) warnPrintln(formatArrayText(WARN_LABEL, format, args)) else Unit

    override fun SpiralContext.info(format: String) = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format)) else Unit
    override fun SpiralContext.info(format: String, arg: Any) = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format, arg)) else Unit
    override fun SpiralContext.info(format: String, th: Throwable) = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format, th)) else Unit
    override fun SpiralContext.info(format: String, arg1: Any, arg2: Any) = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.info(format: String, vararg args: Any) = if (isInfoEnabled) infoPrintln(formatArrayText(INFO_LABEL, format, args)) else Unit
    override fun SpiralContext.infoArray(format: String, args: Array<out Any>) = if (isInfoEnabled) infoPrintln(formatArrayText(INFO_LABEL, format, args)) else Unit

    override fun SpiralContext.debug(format: String) = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format)) else Unit
    override fun SpiralContext.debug(format: String, arg: Any) = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format, arg)) else Unit
    override fun SpiralContext.debug(format: String, th: Throwable) = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format, th)) else Unit
    override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any) = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.debug(format: String, vararg args: Any) = if (isDebugEnabled) debugPrintln(formatArrayText(DEBUG_LABEL, format, args)) else Unit
    override fun SpiralContext.debugArray(format: String, args: Array<out Any>) = if (isDebugEnabled) debugPrintln(formatArrayText(DEBUG_LABEL, format, args)) else Unit

    override fun SpiralContext.trace(format: String) = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format)) else Unit
    override fun SpiralContext.trace(format: String, arg: Any) = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format, arg)) else Unit
    override fun SpiralContext.trace(format: String, th: Throwable) = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format, th)) else Unit
    override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any) = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.trace(format: String, vararg args: Any) = if (isTraceEnabled) tracePrintln(formatArrayText(TRACE_LABEL, format, args)) else Unit
    override fun SpiralContext.traceArray(format: String, args: Array<out Any>) = if (isTraceEnabled) tracePrintln(formatArrayText(TRACE_LABEL, format, args)) else Unit

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