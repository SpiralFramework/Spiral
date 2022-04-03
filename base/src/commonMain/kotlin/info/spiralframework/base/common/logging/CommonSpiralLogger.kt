package info.spiralframework.base.common.logging

import info.spiralframework.base.common.SpiralContext

public open class CommonSpiralLogger(public val name: String, public var loggerLevel: Int = ERROR) : SpiralLogger {
    public companion object {
        public const val TRACE: Int = 0b0000001
        public const val DEBUG: Int = 0b0000010
        public const val INFO: Int = 0b0000100
        public const val WARN: Int = 0b0001000
        public const val ERROR: Int = 0b0010000

        public const val TRACE_LABEL: String = "TRACE"
        public const val DEBUG_LABEL: String = "DEBUG"
        public const val INFO_LABEL: String = " INFO"
        public const val WARN_LABEL: String = " WARN"
        public const val ERROR_LABEL: String = "ERROR"
    }

    public constructor(name: String, errorEnabled: Boolean = false, warnEnabled: Boolean = false, infoEnabled: Boolean = false, debugEnabled: Boolean = false, traceEnabled: Boolean = false)
            : this(name, (if (errorEnabled) ERROR else 0) or (if (warnEnabled) WARN else 0) or (if (infoEnabled) INFO else 0) or (if (debugEnabled) DEBUG else 0) or (if (traceEnabled) TRACE else 0))

    override val isTraceEnabled: Boolean
        get() = isLevelEnabled(TRACE)

    override val isDebugEnabled: Boolean
        get() = isLevelEnabled(DEBUG)

    override val isInfoEnabled: Boolean
        get() = isLevelEnabled(INFO)

    override val isWarnEnabled: Boolean
        get() = isLevelEnabled(WARN)

    override val isErrorEnabled: Boolean
        get() = isLevelEnabled(ERROR)

    public var traceEnabled: Boolean
        get() = isLevelEnabled(TRACE)
        set(value) = setLevelEnabled(TRACE, value)

    public var debugEnabled: Boolean
        get() = isLevelEnabled(DEBUG)
        set(value) = setLevelEnabled(DEBUG, value)

    public var infoEnabled: Boolean
        get() = isLevelEnabled(INFO)
        set(value) = setLevelEnabled(INFO, value)

    public var warnEnabled: Boolean
        get() = isLevelEnabled(WARN)
        set(value) = setLevelEnabled(WARN, value)

    public var errorEnabled: Boolean
        get() = isLevelEnabled(ERROR)
        set(value) = setLevelEnabled(ERROR, value)

    @Suppress("NOTHING_TO_INLINE")
    public inline fun isLevelEnabled(level: Int): Boolean = loggerLevel and level == level
    @Suppress("NOTHING_TO_INLINE")
    public inline fun setLevelEnabled(level: Int, enabled: Boolean) {
        loggerLevel = if (enabled) {
            loggerLevel or level
        } else {
            loggerLevel and level.inv()
        }
    }

    protected open val errorPrintln: (String) -> Unit = ::println
    protected open val warnPrintln: (String) -> Unit = ::println
    protected open val infoPrintln: (String) -> Unit = ::println
    protected open val debugPrintln: (String) -> Unit = ::println
    protected open val tracePrintln: (String) -> Unit = ::println

    override fun SpiralContext.error(format: String): Unit = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format)) else Unit
    override fun SpiralContext.error(format: String, arg: Any): Unit = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format, arg)) else Unit
    override fun SpiralContext.error(format: String, th: Throwable): Unit = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format, th)) else Unit
    override fun SpiralContext.error(format: String, arg1: Any, arg2: Any): Unit = if (isErrorEnabled) errorPrintln(formatText(ERROR_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.error(format: String, vararg args: Any): Unit = if (isErrorEnabled) errorPrintln(formatArrayText(ERROR_LABEL, format, args)) else Unit
    override fun SpiralContext.errorArray(format: String, args: Array<out Any>): Unit = if (isErrorEnabled) errorPrintln(formatArrayText(ERROR_LABEL, format, args)) else Unit

    override fun SpiralContext.warn(format: String): Unit = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format)) else Unit
    override fun SpiralContext.warn(format: String, arg: Any): Unit = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format, arg)) else Unit
    override fun SpiralContext.warn(format: String, th: Throwable): Unit = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format, th)) else Unit
    override fun SpiralContext.warn(format: String, arg1: Any, arg2: Any): Unit = if (isWarnEnabled) warnPrintln(formatText(WARN_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.warn(format: String, vararg args: Any): Unit = if (isWarnEnabled) warnPrintln(formatArrayText(WARN_LABEL, format, args)) else Unit
    override fun SpiralContext.warnArray(format: String, args: Array<out Any>): Unit = if (isWarnEnabled) warnPrintln(formatArrayText(WARN_LABEL, format, args)) else Unit

    override fun SpiralContext.info(format: String): Unit = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format)) else Unit
    override fun SpiralContext.info(format: String, arg: Any): Unit = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format, arg)) else Unit
    override fun SpiralContext.info(format: String, th: Throwable): Unit = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format, th)) else Unit
    override fun SpiralContext.info(format: String, arg1: Any, arg2: Any): Unit = if (isInfoEnabled) infoPrintln(formatText(INFO_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.info(format: String, vararg args: Any): Unit = if (isInfoEnabled) infoPrintln(formatArrayText(INFO_LABEL, format, args)) else Unit
    override fun SpiralContext.infoArray(format: String, args: Array<out Any>): Unit = if (isInfoEnabled) infoPrintln(formatArrayText(INFO_LABEL, format, args)) else Unit

    override fun SpiralContext.debug(format: String): Unit = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format)) else Unit
    override fun SpiralContext.debug(format: String, arg: Any): Unit = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format, arg)) else Unit
    override fun SpiralContext.debug(format: String, th: Throwable): Unit = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format, th)) else Unit
    override fun SpiralContext.debug(format: String, arg1: Any, arg2: Any): Unit = if (isDebugEnabled) debugPrintln(formatText(DEBUG_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.debug(format: String, vararg args: Any): Unit = if (isDebugEnabled) debugPrintln(formatArrayText(DEBUG_LABEL, format, args)) else Unit
    override fun SpiralContext.debugArray(format: String, args: Array<out Any>): Unit = if (isDebugEnabled) debugPrintln(formatArrayText(DEBUG_LABEL, format, args)) else Unit

    override fun SpiralContext.trace(format: String): Unit = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format)) else Unit
    override fun SpiralContext.trace(format: String, arg: Any): Unit = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format, arg)) else Unit
    override fun SpiralContext.trace(format: String, th: Throwable): Unit = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format, th)) else Unit
    override fun SpiralContext.trace(format: String, arg1: Any, arg2: Any): Unit = if (isTraceEnabled) tracePrintln(formatText(TRACE_LABEL, format, arg1, arg2)) else Unit
    override fun SpiralContext.trace(format: String, vararg args: Any): Unit = if (isTraceEnabled) tracePrintln(formatArrayText(TRACE_LABEL, format, args)) else Unit
    override fun SpiralContext.traceArray(format: String, args: Array<out Any>): Unit = if (isTraceEnabled) tracePrintln(formatArrayText(TRACE_LABEL, format, args)) else Unit

    private fun SpiralContext.formatText(level: String, format: String): String = formatTextWithName(level, localise(format))
    private fun SpiralContext.formatText(level: String, format: String, arg: Any): String = formatTextWithName(level, localise(format, arg))
    private fun SpiralContext.formatText(level: String, format: String, th: Throwable) = buildString {
        append('[')
        append(name)
        append(" / ")
        append(level)
        append("] ")
        append(localise(format, th))
        append(" - ")
        append(th.message)
    }

    private fun SpiralContext.formatText(level: String, format: String, arg1: Any, arg2: Any): String = formatTextWithName(level, localise(format, arg1, arg2))
    private fun SpiralContext.formatArrayText(level: String, format: String, args: Array<out Any>): String = formatTextWithName(level, localiseArray(format, args))

    private fun formatTextWithName(level: String, msg: String): String = buildString {
        append('[')
        append(name)
        append(" / ")
        append(level)
        append("] ")
        append(msg)
    }
}