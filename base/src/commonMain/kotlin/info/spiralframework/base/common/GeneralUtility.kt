package info.spiralframework.base.common

@ExperimentalUnsignedTypes
infix fun UInt.alignmentNeededFor(alignment: Int): UInt = (toLong() alignmentNeededFor alignment).toUInt()
@ExperimentalUnsignedTypes
infix fun ULong.alignmentNeededFor(alignment: Int): UInt = (toLong() alignmentNeededFor alignment).toUInt()
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