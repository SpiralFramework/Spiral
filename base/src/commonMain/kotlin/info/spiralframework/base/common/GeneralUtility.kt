package info.spiralframework.base.common

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalUnsignedTypes
infix fun UInt.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment
@ExperimentalUnsignedTypes
infix fun ULong.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment
infix fun Int.alignmentNeededFor(alignment: Int): Int = (alignment - this % alignment) % alignment
infix fun Long.alignmentNeededFor(alignment: Int): Int = ((alignment - this % alignment) % alignment).toInt()

@ExperimentalUnsignedTypes
infix fun UInt.alignedTo(alignment: Int): ULong = this + alignmentNeededFor(alignment)
@ExperimentalUnsignedTypes
infix fun ULong.alignedTo(alignment: Int): ULong = this + alignmentNeededFor(alignment)
infix fun Int.alignedTo(alignment: Int): Int = alignmentNeededFor(alignment) + this
infix fun Long.alignedTo(alignment: Int): Long = alignmentNeededFor(alignment) + this

fun ByteArray.toHexString(): String = buildString {
    this@toHexString.forEach { byte ->
        append(byte.toInt()
                .and(0xFF)
                .toString(16)
                .padStart(2, '0'))
    }
}

fun Byte.reverseBits(): Int =
        (((this.toInt() and 0xFF) * 0x0202020202L and 0x010884422010L) % 1023).toInt() and 0xFF

public inline fun <T> T.takeIf(predicate: Boolean): T? {
    return if (predicate) this else null
}

fun ByteArray.foldToInt16LE(): IntArray = IntArray(size / 2) { i -> (this[i * 2 + 1].toInt() and 0xFF shl 8) or (this[i * 2].toInt() and 0xFF) }
fun ByteArray.foldToInt16BE(): IntArray = IntArray(size / 2) { i -> (this[i * 2].toInt() and 0xFF shl 8) or (this[i * 2 + 1].toInt() and 0xFF) }

public fun byteArrayOfHex(vararg elements: Int): ByteArray = ByteArray(elements.size) { i -> elements[i].toByte() }

public inline fun <reified T> Array<out T>.recast(): Array<T> = Array(size, this::get)

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
public inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Number): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element).toLong()
    }
    return sum
}

/** Puts [value] in this, and returns [value] back */
fun <K, V> MutableMap<K, V>.putBack(key: K, value: V): V {
    this[key] = value
    return value
}

inline fun String.trimNulls(): String = trimEnd(NULL_TERMINATOR)

@ExperimentalContracts
public inline fun <T, R> freeze(receiver: T, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return block(receiver)
}