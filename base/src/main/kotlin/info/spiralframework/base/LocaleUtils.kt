package info.spiralframework.base

fun assertAsLocaleArgument(statement: Boolean, illegalArgument: String, vararg illegalParams: Any) {
    if (!statement)
        throw IllegalArgumentException(SpiralLocale.localise(illegalArgument, *illegalParams))
}

inline fun <reified T> locale(illegalArgument: String, vararg illegalParams: Any?): T = T::class.java.getDeclaredConstructor(String::class.java).newInstance(SpiralLocale.localise(illegalArgument, *illegalParams))

/** Prints the given localised [message] to the standard output stream. */
public inline fun printLocale(message: String, vararg args: Any?) {
    System.out.print(SpiralLocale.localise(message, *args))
}

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun printlnLocale(message: String, vararg args: Any?) {
    System.out.println(SpiralLocale.localise(message, *args))
}

/** Prints the given localised [error] to the standard output stream. */
public inline fun printErrLocale(error: String, vararg args: Any?) {
    System.err.print(SpiralLocale.localise(error, *args))
}

/** Prints the given localised [error] and the line separator to the standard output stream. */
public inline fun printlnErrLocale(error: String, vararg args: Any?) {
    System.err.println(SpiralLocale.localise(error, *args))
}