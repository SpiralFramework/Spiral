package info.spiralframework.base.util

import info.spiralframework.base.binding.localiseArray

//TODO: Replace with require

fun assertAsLocaleArgument(statement: Boolean, illegalArgument: String, vararg illegalParams: Any) {
    if (!statement)
        throw IllegalArgumentException(localiseArray(illegalArgument, illegalParams))
}

inline fun <reified T> locale(illegalArgument: String, vararg illegalParams: Any): T = T::class.java.getDeclaredConstructor(String::class.java).newInstance(localiseArray(illegalArgument, illegalParams))

/** Prints the given localised [message] to the standard output stream. */
public inline fun printLocale(message: String, vararg args: Any) {
    print(localiseArray(message, args))
}

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun printlnLocale(message: String, vararg args: Any) {
    println(localiseArray(message, args))
}

/** Prints the given localised [error] to the standard output stream. */
public inline fun printErrLocale(error: String, vararg args: Any) {
    System.err.print(localiseArray(error, args))
}

/** Prints the given localised [error] and the line separator to the standard output stream. */
public inline fun printlnErrLocale(error: String, vararg args: Any) {
    System.err.println(localiseArray(error, args))
}