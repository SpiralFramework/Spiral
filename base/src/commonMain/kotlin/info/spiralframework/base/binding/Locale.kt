package info.spiralframework.base.binding

import info.spiralframework.base.common.locale.AbstractSpiralLocale
import info.spiralframework.base.common.locale.CommonLocale

expect class SpiralLogger(name: String) {
    fun error(format: String)
    fun error(format: String, arg: Any)
    fun error(format: String, th: Throwable)
    fun error(format: String, arg1: Any, arg2: Any)
    fun error(format: String, vararg args: Any)
    fun errorArray(format: String, args: Array<out Any>)

    fun warn(format: String)
    fun warn(format: String, arg: Any)
    fun warn(format: String, th: Throwable)
    fun warn(format: String, arg1: Any, arg2: Any)
    fun warn(format: String, vararg args: Any)
    fun warnArray(format: String, args: Array<out Any>)

    fun info(format: String)
    fun info(format: String, arg: Any)
    fun info(format: String, th: Throwable)
    fun info(format: String, arg1: Any, arg2: Any)
    fun info(format: String, vararg args: Any)
    fun infoArray(format: String, args: Array<out Any>)

    fun debug(format: String)
    fun debug(format: String, arg: Any)
    fun debug(format: String, th: Throwable)
    fun debug(format: String, arg1: Any, arg2: Any)
    fun debug(format: String, vararg args: Any)
    fun debugArray(format: String, args: Array<out Any>)

    fun trace(format: String)
    fun trace(format: String, arg: Any)
    fun trace(format: String, th: Throwable)
    fun trace(format: String, arg1: Any, arg2: Any)
    fun trace(format: String, vararg args: Any)
    fun traceArray(format: String, args: Array<out Any>)
}

expect object SpiralLocale: AbstractSpiralLocale {
    override fun localise(msg: String): String
    override fun localise(msg: String, arg: Any): String
    override fun localise(msg: String, arg1: Any, arg2: Any): String
    override fun localise(msg: String, vararg args: Any): String
    override fun localiseArray(msg: String, args: Array<out Any>): String

    override fun localiseEnglish(msg: String): String
    override fun localiseEnglish(msg: String, arg: Any): String
    override fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String
    override fun localiseEnglish(msg: String, vararg args: Any): String
    override fun localiseEnglishArray(msg: String, args: Array<out Any>): String
}

expect fun localise(msg: String): String
expect fun localise(msg: String, arg: Any): String
expect fun localise(msg: String, arg1: Any, arg2: Any): String
expect fun localise(msg: String, vararg args: Any): String
expect fun localiseArray(msg: String, args: Array<out Any>): String

expect fun localiseEnglish(msg: String): String
expect fun localiseEnglish(msg: String, arg: Any): String
expect fun localiseEnglish(msg: String, arg1: Any, arg2: Any): String
expect fun localiseEnglish(msg: String, vararg args: Any): String
expect fun localiseEnglishArray(msg: String, args: Array<out Any>): String

internal expect fun defaultLocale(): CommonLocale