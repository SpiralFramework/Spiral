@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.locale

import info.spiralframework.base.binding.printlnErr

public const val AFFIRMATIVE: Boolean = true
public const val NEGATIVE: Boolean = false

/** Prints the given localised [message] to the standard output stream. */
public inline fun SpiralLocale.printLocale(message: String, vararg args: Any): Unit = print(localiseArray(message, args))

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun SpiralLocale.printlnLocale(message: String, vararg args: Any): Unit = println(localiseArray(message, args))

/** Prints the given localised [message] and the line separator to the standard output stream. */
public inline fun SpiralLocale.printlnErrLocale(message: String, vararg args: Any): Unit = printlnErr(localiseArray(message, args))

public fun stripJavaQuirks(str: String): String = str.replace("''", "'")