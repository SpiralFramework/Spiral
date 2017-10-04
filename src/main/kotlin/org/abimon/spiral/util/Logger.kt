package org.abimon.spiral.util

import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.collections.copyFrom

enum class LoggerLevel(val logFunc: (Any?) -> Unit) {
    NONE(::println),
    ERROR(System.err::println),
    WARN(::println),
    INFO(::println),
    DEBUG(::println),
    TRACE(::println);

    operator fun invoke(msg: Any?) = logFunc(msg)
}

fun log(msg: Any?, level: LoggerLevel) {
    if(SpiralModel.loggerLevel >= level)
        level(msg)
}

fun logWithCaller(msg: Any?, level: LoggerLevel) {
    if(SpiralModel.loggerLevel >= level) {
        val there = Thread.currentThread().stackTrace.copyFrom(1).firstOrNull { it.className != "org.abimon.spiral.util.LoggerKt" && !it.className.contains('$') } ?: run {
            level("[Unknown] $msg")

            return@logWithCaller
        }

        val className = run {
            try {
                return@run Class.forName(there.className).simpleName
            } catch (notFound: ClassNotFoundException) {
                return@run there.className
            }
        }

        level("[$className -> ${there.methodName}] $msg")
    }
}

fun debug(msg: Any?) = log(msg, LoggerLevel.DEBUG)

fun trace(msg: Any?) = log(msg, LoggerLevel.TRACE)

fun debugWithCaller(msg: Any?) = logWithCaller(msg, LoggerLevel.DEBUG)
fun traceWithCaller(msg: Any?) = logWithCaller(msg, LoggerLevel.TRACE)