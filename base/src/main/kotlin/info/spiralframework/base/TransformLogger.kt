package info.spiralframework.base

import org.slf4j.Logger
import org.slf4j.Marker

open class TransformLogger(val logger: Logger, val errorTransform: XTransform<String>, val warnTransform: XTransform<String>, val infoTransform: XTransform<String>, val debugTransform: XTransform<String>, val traceTransform: XTransform<String>): Logger by logger {
    constructor(logger: Logger, transform: XTransform<String>): this(logger, transform, transform, transform, transform, transform)
    /** ERROR */

    override fun error(format: String, arg: Any?) = logger.error(errorTransform(format), arg)
    override fun error(marker: Marker?, msg: String) = logger.error(marker, errorTransform(msg))
    override fun error(marker: Marker?, format: String, vararg arguments: Any?) = logger.error(marker, errorTransform(format), *arguments)
    override fun error(msg: String, t: Throwable?) = logger.error(errorTransform(msg), t)
    override fun error(format: String, arg1: Any?, arg2: Any?) = logger.error(errorTransform(format), arg1, arg2)
    override fun error(format: String, vararg arguments: Any?) = logger.error(errorTransform(format), *arguments)
    override fun error(marker: Marker?, msg: String, t: Throwable?) = logger.error(marker, errorTransform(msg), t)
    override fun error(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = logger.error(marker, errorTransform(format), arg1, arg2)
    override fun error(marker: Marker?, format: String, arg: Any?) = logger.error(marker, errorTransform(format), arg)
    override fun error(msg: String) = logger.error(errorTransform(msg))

    /** WARN */

    override fun warn(format: String, arg: Any?) = logger.warn(warnTransform(format), arg)
    override fun warn(marker: Marker?, msg: String) = logger.warn(marker, warnTransform(msg))
    override fun warn(marker: Marker?, format: String, vararg arguments: Any?) = logger.warn(marker, warnTransform(format), *arguments)
    override fun warn(msg: String, t: Throwable?) = logger.warn(warnTransform(msg), t)
    override fun warn(format: String, arg1: Any?, arg2: Any?) = logger.warn(warnTransform(format), arg1, arg2)
    override fun warn(format: String, vararg arguments: Any?) = logger.warn(warnTransform(format), *arguments)
    override fun warn(marker: Marker?, msg: String, t: Throwable?) = logger.warn(marker, warnTransform(msg), t)
    override fun warn(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = logger.warn(marker, warnTransform(format), arg1, arg2)
    override fun warn(marker: Marker?, format: String, arg: Any?) = logger.warn(marker, warnTransform(format), arg)
    override fun warn(msg: String) = logger.warn(warnTransform(msg))

    /** INFO */

    override fun info(format: String, arg: Any?) = logger.info(infoTransform(format), arg)
    override fun info(marker: Marker?, msg: String) = logger.info(marker, infoTransform(msg))
    override fun info(marker: Marker?, format: String, vararg arguments: Any?) = logger.info(marker, infoTransform(format), *arguments)
    override fun info(msg: String, t: Throwable?) = logger.info(infoTransform(msg), t)
    override fun info(format: String, arg1: Any?, arg2: Any?) = logger.info(infoTransform(format), arg1, arg2)
    override fun info(format: String, vararg arguments: Any?) = logger.info(infoTransform(format), *arguments)
    override fun info(marker: Marker?, msg: String, t: Throwable?) = logger.info(marker, infoTransform(msg), t)
    override fun info(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = logger.info(marker, infoTransform(format), arg1, arg2)
    override fun info(marker: Marker?, format: String, arg: Any?) = logger.info(marker, infoTransform(format), arg)
    override fun info(msg: String) = logger.info(infoTransform(msg))

    /** DEBUG */

    override fun debug(format: String, arg: Any?) = logger.debug(debugTransform(format), arg)
    override fun debug(marker: Marker?, msg: String) = logger.debug(marker, debugTransform(msg))
    override fun debug(marker: Marker?, format: String, vararg arguments: Any?) = logger.debug(marker, debugTransform(format), *arguments)
    override fun debug(msg: String, t: Throwable?) = logger.debug(debugTransform(msg), t)
    override fun debug(format: String, arg1: Any?, arg2: Any?) = logger.debug(debugTransform(format), arg1, arg2)
    override fun debug(format: String, vararg arguments: Any?) = logger.debug(debugTransform(format), *arguments)
    override fun debug(marker: Marker?, msg: String, t: Throwable?) = logger.debug(marker, debugTransform(msg), t)
    override fun debug(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = logger.debug(marker, debugTransform(format), arg1, arg2)
    override fun debug(marker: Marker?, format: String, arg: Any?) = logger.debug(marker, debugTransform(format), arg)
    override fun debug(msg: String) = logger.debug(debugTransform(msg))
    
    /** TRACE */

    override fun trace(format: String, arg: Any?) = logger.trace(traceTransform(format), arg)
    override fun trace(marker: Marker?, msg: String) = logger.trace(marker, traceTransform(msg))
    override fun trace(marker: Marker?, format: String, vararg arguments: Any?) = logger.trace(marker, traceTransform(format), *arguments)
    override fun trace(msg: String, t: Throwable?) = logger.trace(traceTransform(msg), t)
    override fun trace(format: String, arg1: Any?, arg2: Any?) = logger.trace(traceTransform(format), arg1, arg2)
    override fun trace(format: String, vararg arguments: Any?) = logger.trace(traceTransform(format), *arguments)
    override fun trace(marker: Marker?, msg: String, t: Throwable?) = logger.trace(marker, traceTransform(msg), t)
    override fun trace(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = logger.trace(marker, traceTransform(format), arg1, arg2)
    override fun trace(marker: Marker?, format: String, arg: Any?) = logger.trace(marker, traceTransform(format), arg)
    override fun trace(msg: String) = logger.trace(traceTransform(msg))
}