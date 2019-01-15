package info.spiralframework.base

import java.nio.ByteBuffer
import java.nio.charset.Charset

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
    get() = info.spiralframework.base.Mode

const val BYTE_NULL_TERMINATOR: Byte = 0

operator fun String.Companion.invoke(bytes: ByteArray, encoding: Charset, mode: Int): String = buildString {
    val capacity = if (mode and Mode.TWO_BYTES_PER_CHARACTER == Mode.TWO_BYTES_PER_CHARACTER) 2 else 1
    val isNullTermed = mode and Mode.NULL_TERMINATED == Mode.NULL_TERMINATED
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

        byteBuffer.flip()
        append(encoding.decode(byteBuffer).get())
        byteBuffer.rewind()
    }
}