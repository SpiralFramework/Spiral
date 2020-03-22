package info.spiralframework.base.jvm

import info.spiralframework.base.common.text.Ansi
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.DecimalFormat
import java.util.*

val spiralModules = arrayOf(
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

val platformModules = arrayOf(
        "commonMain",
        "jvmMain"
)

public inline fun printAndBack(message: String) {
    print(message)
    print(Ansi CURSOR_BACK message.length)
}
/**
 * Performs the given [operation] on each element of this [Iterator].
 * @sample samples.collections.Iterators.forEachIterator
 */
public inline fun <T> Iterator<T>.forEachIndexed(operation: (index: Int, element: T) -> Unit): Unit {
    var index = 0
    for (element in this) operation(index++, element)
}

/**
 * Performs the given [operation] on each element of this [Iterator].
 * @sample samples.collections.Iterators.forEachIterator
 */
public inline fun <T> Iterator<T>.forEachFiltered(filter: (element: T) -> Boolean, operation: (element: T) -> Unit): Unit {
    for (element in this) if (filter(element)) operation(element)
}

object Mode {
    val NULL_TERMINATED = 1
    val TWO_BYTES_PER_CHARACTER = 2

    val NULL_TERMINATED_UTF_16 = 13
}

operator fun String.times(num: Int): String = buildString {
    for (i in 0 until num)
        append(this@times)
}

inline fun ASCIIString(bytes: ByteArray): String = String(bytes, Charsets.US_ASCII)
inline fun UTF8String(bytes: ByteArray): String = String(bytes, Charsets.UTF_8)
inline fun UTF16String(bytes: ByteArray): String = String(bytes, Charsets.UTF_16)
inline fun UTF16StringLE(bytes: ByteArray): String = String(bytes, Charsets.UTF_16LE)
inline fun UTF16StringBE(bytes: ByteArray): String = String(bytes, Charsets.UTF_16BE)

const val FILE_SIZES = "KMGT"
val FILE_SIZES_LOOP = (0 until FILE_SIZES.length).reversed()
val BYTE_POW_INITIAL: Long by lazy { Math.pow(1000.0, FILE_SIZES.length.toDouble()).toLong() }

fun Long.toSize(binary: Boolean = false): Array<Pair<Long, String>> {
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

val FORMAT = DecimalFormat(".##")

fun Long.toSizeString(binary: Boolean = false): String = toSize(binary).joinToString { (bytes, classifier) -> "$bytes $classifier" }
fun Long.toFileSize(binary: Boolean = false): String {
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

fun Throwable.retrieveStackTrace(): String {
    val baos = ByteArrayOutputStream()
    val out = PrintStream(baos)
    printStackTrace(out)

    return String(baos.toByteArray(), Charsets.UTF_8)
}

fun MutableCollection<Int>.addAll(ints: IntArray): Boolean = addAll(ints.toTypedArray())

//Out variance screws up the way Optional works
inline fun <T> Optional<out T>.outOrElse(other: T): T = if (isPresent) get() else other
inline fun <T> Optional<out T>.outOrElseGet(func: () -> T): T = if (isPresent) get() else func()