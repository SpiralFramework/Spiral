package info.spiralframework.base.common

import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.cast
import org.abimon.kornea.errors.common.flatMap
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.SeekableInputFlow
import org.abimon.kornea.io.common.flow.bookmark

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

fun ByteArray.foldToInt16LE(): IntArray = IntArray(size / 2) { i -> (this[i * 2 + 1].toInt() and 0xFF shl 8) or (this[i * 2].toInt() and 0xFF) }
fun ByteArray.foldToInt16BE(): IntArray = IntArray(size / 2) { i -> (this[i * 2].toInt() and 0xFF shl 8) or (this[i * 2 + 1].toInt() and 0xFF) }
fun ByteArray.foldToInt32LE(): IntArray = IntArray(size / 4) { i ->
    (this[i * 4 + 3].toInt() and 0xFF shl 8) or
            (this[i * 4 + 2].toInt() and 0xFF shl 8) or
            (this[i * 4 + 1].toInt() and 0xFF shl 8) or
            (this[i * 4].toInt() and 0xFF) }

/** Puts [value] in this, and returns [value] back */
fun <K, V> MutableMap<K, V>.putBack(key: K, value: V): V {
    this[key] = value
    return value
}

inline fun String.trimNulls(): String = trimEnd(NULL_TERMINATOR)