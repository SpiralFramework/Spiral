package info.spiralframework.base.util

import info.spiralframework.base.binding.localiseArray
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.SpiralLocale

//TODO: Replace with require

@Deprecated("Use require instead", ReplaceWith("require(statement) { localise(illegalArgument, *illegalParams) }"))
fun assertAsLocaleArgument(statement: Boolean, illegalArgument: String, vararg illegalParams: Any) {
    if (!statement)
        throw IllegalArgumentException(localiseArray(illegalArgument, illegalParams))
}

/** Prints the given localised [message] to the standard output stream. */
public inline fun SpiralContext.printLocale(message: String, vararg args: Any) = print(localiseArray(message, args))

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun SpiralContext.printlnLocale(message: String, vararg args: Any) = println(localiseArray(message, args))

/** Prints the given localised [error] to the standard output stream. */
public inline fun printErrLocale(error: String, vararg args: Any) {
    System.err.print(localiseArray(error, args))
}

/** Prints the given localised [error] and the line separator to the standard output stream. */
public inline fun printlnErrLocale(error: String, vararg args: Any) {
    System.err.println(localiseArray(error, args))
}