package info.spiralframework.base.common

@ExperimentalUnsignedTypes
infix fun UInt.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment
@ExperimentalUnsignedTypes
infix fun ULong.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment
infix fun Int.alignmentNeededFor(alignment: Int): Int = (alignment - this % alignment) % alignment
infix fun Long.alignmentNeededFor(alignment: Int): Int = ((alignment - this % alignment) % alignment).toInt()

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