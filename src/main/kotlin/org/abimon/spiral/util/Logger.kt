package org.abimon.spiral.util

import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.collections.copyFrom
import java.io.File
import java.time.Instant

enum class LoggerLevel(val logFunc: (Any?) -> Unit) {
    NONE(::println),
    ERROR(System.err::println),
    WARN(::println),
    INFO(::println),
    DEBUG(::println),
    TRACE(::println);

    operator fun invoke(msg: Any?) = logFunc(msg)
}

val currentLogFile = File("${Instant.now()}.log")
val currentLog = currentLogFile.printWriter()

fun log(msg: Any?, level: LoggerLevel) {
    if(SpiralModel.loggerLevel >= level) {
        level(msg)
        currentLog.println(msg)
    }
}

fun logWithCaller(msg: Any?, level: LoggerLevel) {
    if(SpiralModel.loggerLevel >= level) {
        val there = Thread.currentThread().stackTrace.copyFrom(1).firstOrNull { it.className != "org.abimon.spiral.util.LoggerKt" && !it.className.contains('$') } ?: run {
            level("[Unknown] $msg")
            currentLog.println("[Unknown] $msg")

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
        currentLog.println("[$className -> ${there.methodName}] $msg")
    }
}

fun debug(msg: Any?) = log(msg, LoggerLevel.DEBUG)

fun trace(msg: Any?) = log(msg, LoggerLevel.TRACE)

fun debugWithCaller(msg: Any?) = logWithCaller(msg, LoggerLevel.DEBUG)
fun traceWithCaller(msg: Any?) = logWithCaller(msg, LoggerLevel.TRACE)