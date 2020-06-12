package info.spiralframework.base.common

import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.erorrs.common.cast
import org.abimon.kornea.erorrs.common.flatMap
import org.abimon.kornea.erorrs.common.map
import org.abimon.kornea.io.common.DataCloseable
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.use
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
fun ByteArray.foldToInt32LE(): IntArray = IntArray(size / 4) { i ->
    (this[i * 4 + 3].toInt() and 0xFF shl 8) or
            (this[i * 4 + 2].toInt() and 0xFF shl 8) or
            (this[i * 4 + 1].toInt() and 0xFF shl 8) or
            (this[i * 4].toInt() and 0xFF) }


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

@ExperimentalUnsignedTypes
suspend inline fun <F : InputFlow, reified T> F.fauxSeekFromStartFlatMap(offset: ULong, dataSource: DataSource<out F>, block: (F) -> KorneaResult<T>): KorneaResult<T> {
    val bookmark = position()
    return if (seek(offset.toLong(), InputFlow.FROM_BEGINNING) == null) {
        dataSource.openInputFlow().flatMap { flow ->
            use(flow) {
                flow.skip(offset)
                block(flow)
            }
        }
    } else {
        val result = block(this)
        seek(bookmark.toLong(), InputFlow.FROM_BEGINNING)
        result
    }
}

suspend inline fun <T : DataCloseable, reified R> KorneaResult<T>.useAndMap(block: (T) -> R): KorneaResult<R> =
        if (this is KorneaResult.Success<T>) KorneaResult.Success(value.use(block)) else this.cast()

suspend inline fun <T : DataCloseable, reified R> KorneaResult<T>.useAndFlatMap(block: (T) -> KorneaResult<R>): KorneaResult<R> =
        if (this is KorneaResult.Success<T>) value.use(block) else this.cast()