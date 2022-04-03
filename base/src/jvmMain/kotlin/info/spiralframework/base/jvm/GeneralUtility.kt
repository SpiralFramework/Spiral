@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.jvm

import dev.brella.kornea.errors.common.KorneaResult
import info.spiralframework.base.common.text.Ansi
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow

public val spiralModules: Array<String> = arrayOf(
        "antlr-json",
        "base",
        "base-extended",
        "core",
        "console",
        "gui",
        "formats",
        "osl",
        "osl-2",
        "updater"
)

public val platformModules: Array<String> = arrayOf(
        "commonMain",
        "jvmMain"
)

public inline fun printAndBack(message: String) {
    print(message)
    print(Ansi CURSOR_BACK message.length)
}
/**
 * Performs the given [operation] on each element of this [Iterator].
 */
public inline fun <T> Iterator<T>.forEachIndexed(operation: (index: Int, element: T) -> Unit) {
    var index = 0
    for (element in this) operation(index++, element)
}

/**
 * Performs the given [operation] on each element of this [Iterator].
 */
public inline fun <T> Iterator<T>.forEachFiltered(filter: (element: T) -> Boolean, operation: (element: T) -> Unit) {
    for (element in this) if (filter(element)) operation(element)
}

public object Mode {
    public const val NULL_TERMINATED: Int = 1
    public const val TWO_BYTES_PER_CHARACTER: Int = 2

    public const val NULL_TERMINATED_UTF_16: Int = 13
}

public operator fun String.times(num: Int): String = buildString {
    for (i in 0 until num)
        append(this@times)
}

@Suppress("FunctionName")
public inline fun ASCIIString(bytes: ByteArray): String = String(bytes, Charsets.US_ASCII)
@Suppress("FunctionName")
public inline fun UTF8String(bytes: ByteArray): String = String(bytes, Charsets.UTF_8)
@Suppress("FunctionName")
public inline fun UTF16String(bytes: ByteArray): String = String(bytes, Charsets.UTF_16)
@Suppress("FunctionName")
public inline fun UTF16StringLE(bytes: ByteArray): String = String(bytes, Charsets.UTF_16LE)
@Suppress("FunctionName")
public inline fun UTF16StringBE(bytes: ByteArray): String = String(bytes, Charsets.UTF_16BE)

public const val FILE_SIZES: String = "KMGT"
public val FILE_SIZES_LOOP: IntProgression = FILE_SIZES.indices.reversed()
public val BYTE_POW_INITIAL: Long by lazy { 1000.0.pow(FILE_SIZES.length.toDouble()).toLong() }

public fun Long.toSize(binary: Boolean = false): Array<Pair<Long, String>> {
    val power = if (binary) 1024L else 1000L
    var remaining = this@toSize
    var bytePow = BYTE_POW_INITIAL

    return FILE_SIZES_LOOP.mapNotNull { i ->
        val bytes = remaining / bytePow
        remaining -= (bytes * bytePow)
        bytePow /= power

        if (bytes == 0L)
            return@mapNotNull null
        return@mapNotNull bytes to "${FILE_SIZES[i]}${if (binary) "iB" else "B"}"
    }.let { list -> if (remaining > 0 || list.isEmpty()) list.plus(remaining to "B") else list }.toTypedArray()
}

public val FORMAT: DecimalFormat = DecimalFormat(".##")

public fun Long.toSizeString(binary: Boolean = false): String = toSize(binary).joinToString { (bytes, classifier) -> "$bytes $classifier" }
public fun Long.toFileSize(binary: Boolean = false): String {
//    val ourNumber = this
//    var power = 1L
//    var times = 0
//    while (ourNumber / power > 10) {
//        power *= 10
//        times++
//    }
//
//    return buildString {
//        append(FORMAT.format(ourNumber.toDouble() / power.toDouble()))
//        append(' ')
//        if (times >= 3)
//            append(FILE_SIZES[(times / 3) - 1])
//        append('B')
//    }

    val sizeArray = toSize(binary)
    if (sizeArray.size == 1)
        return sizeArray[0].let { (bytes, prefix) -> "$bytes $prefix" }
    return sizeArray[0].let { (mainBytes, mainPrefix) -> "${FORMAT.format(mainBytes.toDouble() + (sizeArray[1].first.toDouble() / 1000))} $mainPrefix" }
}

public fun Throwable.retrieveStackTrace(): String {
    val baos = ByteArrayOutputStream()
    val out = PrintStream(baos)
    printStackTrace(out)

    return String(baos.toByteArray(), Charsets.UTF_8)
}

public fun MutableCollection<Int>.addAll(ints: IntArray): Boolean = addAll(ints.toTypedArray())

//Out variance screws up the way Optional works
public inline fun <T> Optional<out T>.outOrElse(other: T): T = if (isPresent) get() else other
public inline fun <T> Optional<out T>.outOrElseGet(func: () -> T): T = if (isPresent) get() else func()

public inline fun <reified T> select(prefix: String?, keys: Array<String>, values: Array<T>): KorneaResult<T> = info.spiralframework.base.common.select(::readLine, prefix, keys, values)
public inline fun <reified T> select(prefix: String?, keys: Array<String>, values: Map<String, T>): KorneaResult<T> = info.spiralframework.base.common.select(::readLine, prefix, keys, values)