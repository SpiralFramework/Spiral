package info.spiralframework.base.common

import kotlin.math.abs

/** ULong -> Signed */

@ExperimentalUnsignedTypes
operator fun ULong.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun ULong.plus(other: Int): ULong = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun ULong.plus(other: Short): ULong = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun ULong.plus(other: Byte): ULong = plus(other.toLong())

@ExperimentalUnsignedTypes
operator fun ULong.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun ULong.minus(other: Int): ULong = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun ULong.minus(other: Short): ULong = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun ULong.minus(other: Byte): ULong = minus(other.toLong())

@ExperimentalUnsignedTypes
operator fun ULong.times(other: Long): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.times(other: Int): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.times(other: Short): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.times(other: Byte): ULong = times(other.toULong())

@ExperimentalUnsignedTypes
operator fun ULong.div(other: Long): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.div(other: Int): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.div(other: Short): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.div(other: Byte): ULong = div(other.toULong())

@ExperimentalUnsignedTypes
operator fun ULong.rem(other: Long): ULong = rem(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.rem(other: Int): ULong = rem(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.rem(other: Short): ULong = rem(other.toULong())
@ExperimentalUnsignedTypes
operator fun ULong.rem(other: Byte): ULong = rem(other.toULong())

/** Signed -> ULong */

@ExperimentalUnsignedTypes
operator fun Long.plus(other: ULong): Long = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.plus(other: ULong): Long = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.plus(other: ULong): Long = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.plus(other: ULong): Long = plus(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.minus(other: ULong): Long = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.minus(other: ULong): Long = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.minus(other: ULong): Long = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.minus(other: ULong): Long = minus(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.times(other: ULong): Long = times(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.times(other: ULong): Long = times(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.times(other: ULong): Long = times(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.times(other: ULong): Long = times(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.div(other: ULong): Long = div(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.div(other: ULong): Long = div(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.div(other: ULong): Long = div(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.div(other: ULong): Long = div(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.rem(other: ULong): Long = rem(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.rem(other: ULong): Long = rem(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.rem(other: ULong): Long = rem(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.rem(other: ULong): Long = rem(other.toLong())

/** UInt -> Signed */

@ExperimentalUnsignedTypes
operator fun UInt.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun UInt.plus(other: Int): UInt = if (other > 0) plus(other.toUInt()) else minus(abs(other).toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.plus(other: Short): UInt = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun UInt.plus(other: Byte): UInt = plus(other.toInt())

@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Int): UInt = if (other > 0) minus(other.toUInt()) else plus(abs(other).toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Short): UInt = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Byte): UInt = minus(other.toInt())

@ExperimentalUnsignedTypes
operator fun UInt.times(other: Long): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun UInt.times(other: Int): UInt = times(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.times(other: Short): UInt = times(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.times(other: Byte): UInt = times(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UInt.div(other: Long): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun UInt.div(other: Int): UInt = div(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.div(other: Short): UInt = div(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.div(other: Byte): UInt = div(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Long): ULong = rem(other.toULong())
@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Int): ULong = rem(other.toULong())
@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Short): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Byte): UInt = rem(other.toUInt())

/** Signed -> UInt */

@ExperimentalUnsignedTypes
operator fun Long.plus(other: UInt): Long = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.plus(other: UInt): Long = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.plus(other: UInt): Long = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.plus(other: UInt): Long = plus(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.minus(other: UInt): Long = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.minus(other: UInt): Long = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.minus(other: UInt): Long = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.minus(other: UInt): Long = minus(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.times(other: UInt): Long = times(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.times(other: UInt): Long = times(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.times(other: UInt): Long = times(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.times(other: UInt): Long = times(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.div(other: UInt): Long = div(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.div(other: UInt): Long = div(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.div(other: UInt): Long = div(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.div(other: UInt): Long = div(other.toLong())

@ExperimentalUnsignedTypes
operator fun Long.rem(other: UInt): Long = rem(other.toLong())
@ExperimentalUnsignedTypes
operator fun Int.rem(other: UInt): Long = rem(other.toLong())
@ExperimentalUnsignedTypes
operator fun Short.rem(other: UInt): Long = rem(other.toLong())
@ExperimentalUnsignedTypes
operator fun Byte.rem(other: UInt): Long = rem(other.toLong())

/** UShort -> Signed */

@ExperimentalUnsignedTypes
operator fun UShort.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun UShort.plus(other: Int): UInt = if (other > 0) plus(other.toUInt()) else minus(abs(other).toUInt())
@ExperimentalUnsignedTypes
operator fun UShort.plus(other: Short): UInt = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun UShort.plus(other: Byte): UInt = plus(other.toShort())

@ExperimentalUnsignedTypes
operator fun UShort.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun UShort.minus(other: Int): UInt = if (other > 0) minus(other.toUInt()) else plus(abs(other).toUInt())
@ExperimentalUnsignedTypes
operator fun UShort.minus(other: Short): UInt = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun UShort.minus(other: Byte): UInt = minus(other.toInt())

@ExperimentalUnsignedTypes
operator fun UShort.times(other: Long): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun UShort.times(other: Int): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun UShort.times(other: Short): UInt = times(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UShort.times(other: Byte): UInt = times(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UShort.div(other: Long): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun UShort.div(other: Int): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun UShort.div(other: Short): UInt = div(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UShort.div(other: Byte): UInt = div(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UShort.rem(other: Long): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UShort.rem(other: Int): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UShort.rem(other: Short): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UShort.rem(other: Byte): UInt = rem(other.toUInt())

/** Signed -> UShort */

@ExperimentalUnsignedTypes
operator fun Long.plus(other: UShort): Long = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.plus(other: UShort): Int = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.plus(other: UShort): Int = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.plus(other: UShort): Int = plus(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.minus(other: UShort): Long = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.minus(other: UShort): Int = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.minus(other: UShort): Int = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.minus(other: UShort): Int = minus(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.times(other: UShort): Long = times(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.times(other: UShort): Int = times(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.times(other: UShort): Int = times(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.times(other: UShort): Int = times(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.div(other: UShort): Long = div(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.div(other: UShort): Int = div(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.div(other: UShort): Int = div(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.div(other: UShort): Int = div(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.rem(other: UShort): Long = rem(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.rem(other: UShort): Int = rem(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.rem(other: UShort): Int = rem(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.rem(other: UShort): Int = rem(other.toInt())

/** UByte -> Signed */

@ExperimentalUnsignedTypes
operator fun UByte.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun UByte.plus(other: Int): UInt = if (other > 0) plus(other.toUInt()) else minus(abs(other).toUInt())
@ExperimentalUnsignedTypes
operator fun UByte.plus(other: Short): UInt = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun UByte.plus(other: Byte): UInt = plus(other.toShort())

@ExperimentalUnsignedTypes
operator fun UByte.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
@ExperimentalUnsignedTypes
operator fun UByte.minus(other: Int): UInt = if (other > 0) minus(other.toUInt()) else plus(abs(other).toUInt())
@ExperimentalUnsignedTypes
operator fun UByte.minus(other: Short): UInt = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun UByte.minus(other: Byte): UInt = minus(other.toInt())

@ExperimentalUnsignedTypes
operator fun UByte.times(other: Long): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun UByte.times(other: Int): ULong = times(other.toULong())
@ExperimentalUnsignedTypes
operator fun UByte.times(other: Short): UInt = times(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UByte.times(other: Byte): UInt = times(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UByte.div(other: Long): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun UByte.div(other: Int): ULong = div(other.toULong())
@ExperimentalUnsignedTypes
operator fun UByte.div(other: Short): UInt = div(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UByte.div(other: Byte): UInt = div(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UByte.rem(other: Long): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UByte.rem(other: Int): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UByte.rem(other: Short): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UByte.rem(other: Byte): UInt = rem(other.toUInt())

/** Signed -> UByte */

@ExperimentalUnsignedTypes
operator fun Long.plus(other: UByte): Long = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.plus(other: UByte): Int = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.plus(other: UByte): Int = plus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.plus(other: UByte): Int = plus(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.minus(other: UByte): Long = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.minus(other: UByte): Int = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.minus(other: UByte): Int = minus(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.minus(other: UByte): Int = minus(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.times(other: UByte): Long = times(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.times(other: UByte): Int = times(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.times(other: UByte): Int = times(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.times(other: UByte): Int = times(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.div(other: UByte): Long = div(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.div(other: UByte): Int = div(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.div(other: UByte): Int = div(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.div(other: UByte): Int = div(other.toInt())

@ExperimentalUnsignedTypes
operator fun Long.rem(other: UByte): Long = rem(other.toInt())
@ExperimentalUnsignedTypes
operator fun Int.rem(other: UByte): Int = rem(other.toInt())
@ExperimentalUnsignedTypes
operator fun Short.rem(other: UByte): Int = rem(other.toInt())
@ExperimentalUnsignedTypes
operator fun Byte.rem(other: UByte): Int = rem(other.toInt())