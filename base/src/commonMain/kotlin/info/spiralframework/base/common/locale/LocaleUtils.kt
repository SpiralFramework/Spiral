package info.spiralframework.base.common.locale

import info.spiralframework.base.binding.printlnErr
import info.spiralframework.base.common.SpiralContext

const val AFFIRMATIVE = true
const val NEGATIVE = false

/** Prints the given localised [message] to the standard output stream. */
public inline fun SpiralContext.printLocale(message: String, vararg args: Any) = print(localiseArray(message, args))

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun SpiralContext.printlnLocale(message: String, vararg args: Any) = println(localiseArray(message, args))

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun SpiralContext.printlnErrLocale(message: String, vararg args: Any) = printlnErr(localiseArray(message, args))
