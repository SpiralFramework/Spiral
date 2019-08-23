package info.spiralframework.base.util

import info.spiralframework.base.ANSI
import info.spiralframework.base.MappingIterator
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.text.DecimalFormat

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Any?) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Int) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Long) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Byte) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Short) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Char) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Boolean) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Float) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: Double) {
    System.err.print(error)
}

/** Prints the given [error] to the standard error stream. */
public inline fun printErr(error: CharArray) {
    System.err.print(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Any?) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Int) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Long) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Byte) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Short) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Char) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Boolean) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Float) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: Double) {
    System.err.println(error)
}

/** Prints the given [error] and the line separator to the standard error stream. */
public inline fun printlnErr(error: CharArray) {
    System.err.println(error)
}


public inline fun printAndBack(message: String) {
    System.out.print(message)
    System.out.print(ANSI CURSOR_BACK message.length)
}

public fun <T, V> Array<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)
public fun <T, V> List<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)
public fun <T, V> java.util.Enumeration<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)

/**
 * Performs the given [operation] on each element of this [Iterator].
 * @sample samples.collections.Iterators.forEachIterator
 */
public inline fun <T> Iterator<T>.forEachIndexed(operation: (index: Int, T) -> Unit): Unit {
    var index = 0
    for (element in this) operation(index++, element)
}

/**
 * Performs the given [operation] on each element of this [Iterator].
 * @sample samples.collections.Iterators.forEachIterator
 */
public inline fun <T> Iterator<T>.forEachFiltered(filter: (T) -> Boolean, operation: (T) -> Unit): Unit {
    for (element in this) if (filter(element)) operation(element)
}

/**
 * Executes the given [block] and returns elapsed time in milliseconds.
 */
public inline fun <T> measureResultTimeMillis(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)}

/**
 * Executes the given [block] and returns elapsed time in nanoseconds.
 */
public inline fun <T> measureResultNanoTime(block: () -> T): Pair<T, Long> {
    val start = System.nanoTime()
    val result = block()
    return result to (System.nanoTime() - start)
}

object Mode {
    val NULL_TERMINATED = 1
    val TWO_BYTES_PER_CHARACTER = 2

    val NULL_TERMINATED_UTF_16 = 13
}

val String.Companion.Mode: Mode
    get() = info.spiralframework.base.util.Mode

const val BYTE_NULL_TERMINATOR: Byte = 0

operator fun String.Companion.invoke(bytes: ByteArray, encoding: Charset, mode: Int): String = buildString {
    val capacity = if (mode and info.spiralframework.base.util.Mode.TWO_BYTES_PER_CHARACTER == info.spiralframework.base.util.Mode.TWO_BYTES_PER_CHARACTER) 2 else 1
    val isNullTermed = mode and info.spiralframework.base.util.Mode.NULL_TERMINATED == info.spiralframework.base.util.Mode.NULL_TERMINATED
    val byteBuffer = ByteBuffer.allocate(capacity)
    var countedNullTerms = 0
    var byte: Byte = 0

    for (i in 0 until bytes.size step capacity) {
        for (j in 0 until capacity) {
            byte = bytes[i + j]
            byteBuffer.put(bytes[i + j])

            if (isNullTermed && byte == BYTE_NULL_TERMINATOR)
                countedNullTerms++
        }

        if (isNullTermed && countedNullTerms == capacity)
            break

        byteBuffer.flipSafe()
        append(encoding.decode(byteBuffer).get())
        byteBuffer.rewindSafe()
    }
}

operator fun String.times(num: Int): String = buildString {
    for(i in 0 until num)
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