package info.spiralframework.base.util

import info.spiralframework.base.common.SpiralContext

//TODO: Replace with require

@Deprecated("Use require instead", ReplaceWith("require(statement) { localise(illegalArgument, *illegalParams) }"))
fun assertAsLocaleArgument(statement: Boolean, illegalArgument: String, vararg illegalParams: Any): Nothing =
        throw IllegalStateException("Localisation requires a context")

/** Prints the given localised [message] to the standard output stream. */
public inline fun SpiralContext.printLocale(message: String, vararg args: Any) = print(localiseArray(message, args))

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun SpiralContext.printlnLocale(message: String, vararg args: Any) = println(localiseArray(message, args))

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun SpiralContext.printlnErrLocale(message: String, vararg args: Any) = System.err.println(localiseArray(message, args))

/** Prints the given localised [error] to the standard output stream. */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Localisation requires a context")
public inline fun printErrLocale(error: String, vararg args: Any): Nothing =
        throw IllegalStateException("Localisation requires a context")

/** Prints the given localised [error] and the line separator to the standard output stream. */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Localisation requires a context")
public inline fun printlnErrLocale(error: String, vararg args: Any): Nothing =
        throw IllegalStateException("Localisation requires a context")