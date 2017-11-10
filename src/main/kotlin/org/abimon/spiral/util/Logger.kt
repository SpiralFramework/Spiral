package org.abimon.spiral.util

import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.collections.copyFrom
import java.io.File

enum class LoggerLevel(val logFunc: (Any?) -> Unit) {
    NONE(::println),
    ERROR(System.err::println),
    WARN(::println),
    INFO(::println),
    DEBUG(::println),
    TRACE(::println);

    val enabled: Boolean
        get() = SpiralModel.loggerLevel >= this

    operator fun invoke(msg: Any?) = logFunc(msg)
}

val currentLogFile = File("spiral.log")
val currentLog = currentLogFile.printWriter()

fun log(msg: Any?, level: LoggerLevel) {
    if(SpiralModel.loggerLevel >= level) {
        level(msg)
        currentLog.println(msg)
    }
}

fun logWithCaller(msg: Any?, level: LoggerLevel, stepsDown: Int) {
    if(SpiralModel.loggerLevel >= level) {
        val there = Thread.currentThread().stackTrace.copyFrom(1 + stepsDown).firstOrNull { it.className != "org.abimon.spiral.util.LoggerKt" && !it.className.contains('$') } ?: run {
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

fun debugWithCaller(msg: Any?, stepsDown: Int = 0) = logWithCaller(msg, LoggerLevel.DEBUG, stepsDown)
fun traceWithCaller(msg: Any?, stepsDown: Int = 0) = logWithCaller(msg, LoggerLevel.TRACE, stepsDown)