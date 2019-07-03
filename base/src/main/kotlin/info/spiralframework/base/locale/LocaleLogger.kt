package info.spiralframework.base.locale

import org.slf4j.Logger
import org.slf4j.Marker

open class LocaleLogger(val logger: Logger) : Logger by logger {
    /** ERROR */

    override fun error(format: String, arg: Any) {
        if (logger.isErrorEnabled) {
            logger.error(SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun error(marker: Marker?, msg: String) {
        if (logger.isErrorEnabled(marker)) {
            logger.error(marker, msg)
        }
    }
    override fun error(marker: Marker?, format: String, vararg arguments: Any) {
        if (logger.isErrorEnabled(marker)) {
            logger.error(marker, SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun error(msg: String, t: Throwable?) {
        if (logger.isErrorEnabled) {
            logger.error(SpiralLocale.localiseString(msg), t)
        }
    }
    override fun error(format: String, arg1: Any, arg2: Any) {
        if (logger.isErrorEnabled) {
            logger.error(SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun error(format: String, vararg arguments: Any) {
        if (logger.isErrorEnabled) {
            logger.error(SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun error(marker: Marker?, msg: String, t: Throwable?) {
        if (logger.isErrorEnabled(marker)) {
            logger.error(marker, msg, t)
        }
    }
    override fun error(marker: Marker?, format: String, arg1: Any, arg2: Any) {
        if (logger.isErrorEnabled(marker)) {
            logger.error(marker, SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun error(marker: Marker?, format: String, arg: Any) {
        if (logger.isErrorEnabled(marker)) {
            logger.error(marker, SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun error(msg: String) {
        if (logger.isErrorEnabled) logger.error(msg)
    }

    /** WARN */

    override fun warn(format: String, arg: Any) {
        if (logger.isWarnEnabled) {
            logger.warn(SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun warn(marker: Marker?, msg: String) {
        if (logger.isWarnEnabled(marker)) {
            logger.warn(marker, msg)
        }
    }
    override fun warn(marker: Marker?, format: String, vararg arguments: Any) {
        if (logger.isWarnEnabled(marker)) {
            logger.warn(marker, SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun warn(msg: String, t: Throwable?) {
        if (logger.isWarnEnabled) {
            logger.warn(SpiralLocale.localiseString(msg), t)
        }
    }
    override fun warn(format: String, arg1: Any, arg2: Any) {
        if (logger.isWarnEnabled) {
            logger.warn(SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun warn(format: String, vararg arguments: Any) {
        if (logger.isWarnEnabled) {
            logger.warn(SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun warn(marker: Marker?, msg: String, t: Throwable?) {
        if (logger.isWarnEnabled(marker)) {
            logger.warn(marker, msg, t)
        }
    }
    override fun warn(marker: Marker?, format: String, arg1: Any, arg2: Any) {
        if (logger.isWarnEnabled(marker)) {
            logger.warn(marker, SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun warn(marker: Marker?, format: String, arg: Any) {
        if (logger.isWarnEnabled(marker)) {
            logger.warn(marker, SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun warn(msg: String) {
        if (logger.isWarnEnabled) logger.warn(msg)
    }

    /** INFO */

    override fun info(format: String, arg: Any) {
        if (logger.isInfoEnabled) {
            logger.info(SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun info(marker: Marker?, msg: String) {
        if (logger.isInfoEnabled(marker)) {
            logger.info(marker, msg)
        }
    }
    override fun info(marker: Marker?, format: String, vararg arguments: Any) {
        if (logger.isInfoEnabled(marker)) {
            logger.info(marker, SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun info(msg: String, t: Throwable?) {
        if (logger.isInfoEnabled) {
            logger.info(SpiralLocale.localiseString(msg), t)
        }
    }
    override fun info(format: String, arg1: Any, arg2: Any) {
        if (logger.isInfoEnabled) {
            logger.info(SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun info(format: String, vararg arguments: Any) {
        if (logger.isInfoEnabled) {
            logger.info(SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun info(marker: Marker?, msg: String, t: Throwable?) {
        if (logger.isInfoEnabled(marker)) {
            logger.info(marker, msg, t)
        }
    }
    override fun info(marker: Marker?, format: String, arg1: Any, arg2: Any) {
        if (logger.isInfoEnabled(marker)) {
            logger.info(marker, SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun info(marker: Marker?, format: String, arg: Any) {
        if (logger.isInfoEnabled(marker)) {
            logger.info(marker, SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun info(msg: String) {
        if (logger.isInfoEnabled) logger.info(msg)
    }

    /** DEBUG */

    override fun debug(format: String, arg: Any) {
        if (logger.isDebugEnabled) {
            logger.debug(SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun debug(marker: Marker?, msg: String) {
        if (logger.isDebugEnabled(marker)) {
            logger.debug(marker, msg)
        }
    }
    override fun debug(marker: Marker?, format: String, vararg arguments: Any) {
        if (logger.isDebugEnabled(marker)) {
            logger.debug(marker, SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun debug(msg: String, t: Throwable?) {
        if (logger.isDebugEnabled) {
            logger.debug(SpiralLocale.localiseString(msg), t)
        }
    }
    override fun debug(format: String, arg1: Any, arg2: Any) {
        if (logger.isDebugEnabled) {
            logger.debug(SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun debug(format: String, vararg arguments: Any) {
        if (logger.isDebugEnabled) {
            logger.debug(SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun debug(marker: Marker?, msg: String, t: Throwable?) {
        if (logger.isDebugEnabled(marker)) {
            logger.debug(marker, msg, t)
        }
    }
    override fun debug(marker: Marker?, format: String, arg1: Any, arg2: Any) {
        if (logger.isDebugEnabled(marker)) {
            logger.debug(marker, SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun debug(marker: Marker?, format: String, arg: Any) {
        if (logger.isDebugEnabled(marker)) {
            logger.debug(marker, SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun debug(msg: String) {
        if (logger.isDebugEnabled) logger.debug(msg)
    }
    
    /** TRACE */

    override fun trace(format: String, arg: Any) {
        if (logger.isTraceEnabled) {
            logger.trace(SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun trace(marker: Marker?, msg: String) {
        if (logger.isTraceEnabled(marker)) {
            logger.trace(marker, msg)
        }
    }
    override fun trace(marker: Marker?, format: String, vararg arguments: Any) {
        if (logger.isTraceEnabled(marker)) {
            logger.trace(marker, SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun trace(msg: String, t: Throwable?) {
        if (logger.isTraceEnabled) {
            logger.trace(SpiralLocale.localiseString(msg), t)
        }
    }
    override fun trace(format: String, arg1: Any, arg2: Any) {
        if (logger.isTraceEnabled) {
            logger.trace(SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun trace(format: String, vararg arguments: Any) {
        if (logger.isTraceEnabled) {
            logger.trace(SpiralLocale.localise(format, *arguments), arguments.lastOrNull())
        }
    }
    override fun trace(marker: Marker?, msg: String, t: Throwable?) {
        if (logger.isTraceEnabled(marker)) {
            logger.trace(marker, msg, t)
        }
    }
    override fun trace(marker: Marker?, format: String, arg1: Any, arg2: Any) {
        if (logger.isTraceEnabled(marker)) {
            logger.trace(marker, SpiralLocale.localise(format, arg1, arg2), arg2)
        }
    }
    override fun trace(marker: Marker?, format: String, arg: Any) {
        if (logger.isTraceEnabled(marker)) {
            logger.trace(marker, SpiralLocale.localise(format, arg), arg)
        }
    }
    override fun trace(msg: String) {
        if (logger.isTraceEnabled) logger.trace(msg)
    }
}