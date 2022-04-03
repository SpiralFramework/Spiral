package info.spiralframework.base.common

import kotlin.math.abs

/** ULong -> Signed */

public operator fun ULong.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
public operator fun ULong.plus(other: Int): ULong = plus(other.toLong())
public operator fun ULong.plus(other: Short): ULong = plus(other.toLong())
public operator fun ULong.plus(other: Byte): ULong = plus(other.toLong())

public operator fun ULong.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
public operator fun ULong.minus(other: Int): ULong = minus(other.toLong())
public operator fun ULong.minus(other: Short): ULong = minus(other.toLong())
public operator fun ULong.minus(other: Byte): ULong = minus(other.toLong())

public operator fun ULong.times(other: Long): ULong = times(other.toULong())
public operator fun ULong.times(other: Int): ULong = times(other.toULong())
public operator fun ULong.times(other: Short): ULong = times(other.toULong())
public operator fun ULong.times(other: Byte): ULong = times(other.toULong())

public operator fun ULong.div(other: Long): ULong = div(other.toULong())
public operator fun ULong.div(other: Int): ULong = div(other.toULong())
public operator fun ULong.div(other: Short): ULong = div(other.toULong())
public operator fun ULong.div(other: Byte): ULong = div(other.toULong())

public operator fun ULong.rem(other: Long): ULong = rem(other.toULong())
public operator fun ULong.rem(other: Int): ULong = rem(other.toULong())
public operator fun ULong.rem(other: Short): ULong = rem(other.toULong())
public operator fun ULong.rem(other: Byte): ULong = rem(other.toULong())

/** Signed -> ULong */

public operator fun Long.plus(other: ULong): Long = plus(other.toLong())
public operator fun Int.plus(other: ULong): Long = plus(other.toLong())
public operator fun Short.plus(other: ULong): Long = plus(other.toLong())
public operator fun Byte.plus(other: ULong): Long = plus(other.toLong())

public operator fun Long.minus(other: ULong): Long = minus(other.toLong())
public operator fun Int.minus(other: ULong): Long = minus(other.toLong())
public operator fun Short.minus(other: ULong): Long = minus(other.toLong())
public operator fun Byte.minus(other: ULong): Long = minus(other.toLong())

public operator fun Long.times(other: ULong): Long = times(other.toLong())
public operator fun Int.times(other: ULong): Long = times(other.toLong())
public operator fun Short.times(other: ULong): Long = times(other.toLong())
public operator fun Byte.times(other: ULong): Long = times(other.toLong())

public operator fun Long.div(other: ULong): Long = div(other.toLong())
public operator fun Int.div(other: ULong): Long = div(other.toLong())
public operator fun Short.div(other: ULong): Long = div(other.toLong())
public operator fun Byte.div(other: ULong): Long = div(other.toLong())

public operator fun Long.rem(other: ULong): Long = rem(other.toLong())
public operator fun Int.rem(other: ULong): Long = rem(other.toLong())
public operator fun Short.rem(other: ULong): Long = rem(other.toLong())
public operator fun Byte.rem(other: ULong): Long = rem(other.toLong())

/** UInt -> Signed */

public operator fun UInt.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
public operator fun UInt.plus(other: Int): UInt = if (other > 0) plus(other.toUInt()) else minus(abs(other).toUInt())
public operator fun UInt.plus(other: Short): UInt = plus(other.toInt())
public operator fun UInt.plus(other: Byte): UInt = plus(other.toInt())

public operator fun UInt.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
public operator fun UInt.minus(other: Int): UInt = if (other > 0) minus(other.toUInt()) else plus(abs(other).toUInt())
public operator fun UInt.minus(other: Short): UInt = minus(other.toInt())
public operator fun UInt.minus(other: Byte): UInt = minus(other.toInt())

public operator fun UInt.times(other: Long): ULong = times(other.toULong())
public operator fun UInt.times(other: Int): UInt = times(other.toUInt())
public operator fun UInt.times(other: Short): UInt = times(other.toUInt())
public operator fun UInt.times(other: Byte): UInt = times(other.toUInt())

public operator fun UInt.div(other: Long): ULong = div(other.toULong())
public operator fun UInt.div(other: Int): UInt = div(other.toUInt())
public operator fun UInt.div(other: Short): UInt = div(other.toUInt())
public operator fun UInt.div(other: Byte): UInt = div(other.toUInt())

public operator fun UInt.rem(other: Long): ULong = rem(other.toULong())
public operator fun UInt.rem(other: Int): ULong = rem(other.toULong())
public operator fun UInt.rem(other: Short): UInt = rem(other.toUInt())
public operator fun UInt.rem(other: Byte): UInt = rem(other.toUInt())

/** Signed -> UInt */

public operator fun Long.plus(other: UInt): Long = plus(other.toLong())
public operator fun Int.plus(other: UInt): Long = plus(other.toLong())
public operator fun Short.plus(other: UInt): Long = plus(other.toLong())
public operator fun Byte.plus(other: UInt): Long = plus(other.toLong())

public operator fun Long.minus(other: UInt): Long = minus(other.toLong())
public operator fun Int.minus(other: UInt): Long = minus(other.toLong())
public operator fun Short.minus(other: UInt): Long = minus(other.toLong())
public operator fun Byte.minus(other: UInt): Long = minus(other.toLong())

public operator fun Long.times(other: UInt): Long = times(other.toLong())
public operator fun Int.times(other: UInt): Long = times(other.toLong())
public operator fun Short.times(other: UInt): Long = times(other.toLong())
public operator fun Byte.times(other: UInt): Long = times(other.toLong())

public operator fun Long.div(other: UInt): Long = div(other.toLong())
public operator fun Int.div(other: UInt): Long = div(other.toLong())
public operator fun Short.div(other: UInt): Long = div(other.toLong())
public operator fun Byte.div(other: UInt): Long = div(other.toLong())

public operator fun Long.rem(other: UInt): Long = rem(other.toLong())
public operator fun Int.rem(other: UInt): Long = rem(other.toLong())
public operator fun Short.rem(other: UInt): Long = rem(other.toLong())
public operator fun Byte.rem(other: UInt): Long = rem(other.toLong())

/** UShort -> Signed */

public operator fun UShort.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
public operator fun UShort.plus(other: Int): UInt = if (other > 0) plus(other.toUInt()) else minus(abs(other).toUInt())
public operator fun UShort.plus(other: Short): UInt = plus(other.toInt())
public operator fun UShort.plus(other: Byte): UInt = plus(other.toShort())

@ExperimentalUnsignedTypes
public operator fun UShort.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
public operator fun UShort.minus(other: Int): UInt = if (other > 0) minus(other.toUInt()) else plus(abs(other).toUInt())
public operator fun UShort.minus(other: Short): UInt = minus(other.toInt())
public operator fun UShort.minus(other: Byte): UInt = minus(other.toInt())

public operator fun UShort.times(other: Long): ULong = times(other.toULong())
public operator fun UShort.times(other: Int): ULong = times(other.toULong())
public operator fun UShort.times(other: Short): UInt = times(other.toUInt())
public operator fun UShort.times(other: Byte): UInt = times(other.toUInt())

public operator fun UShort.div(other: Long): ULong = div(other.toULong())
public operator fun UShort.div(other: Int): ULong = div(other.toULong())
public operator fun UShort.div(other: Short): UInt = div(other.toUInt())
public operator fun UShort.div(other: Byte): UInt = div(other.toUInt())

public operator fun UShort.rem(other: Long): UInt = rem(other.toUInt())
public operator fun UShort.rem(other: Int): UInt = rem(other.toUInt())
public operator fun UShort.rem(other: Short): UInt = rem(other.toUInt())
public operator fun UShort.rem(other: Byte): UInt = rem(other.toUInt())

/** Signed -> UShort */

public operator fun Long.plus(other: UShort): Long = plus(other.toInt())
public operator fun Int.plus(other: UShort): Int = plus(other.toInt())
public operator fun Short.plus(other: UShort): Int = plus(other.toInt())
public operator fun Byte.plus(other: UShort): Int = plus(other.toInt())

public operator fun Long.minus(other: UShort): Long = minus(other.toInt())
public operator fun Int.minus(other: UShort): Int = minus(other.toInt())
public operator fun Short.minus(other: UShort): Int = minus(other.toInt())
public operator fun Byte.minus(other: UShort): Int = minus(other.toInt())

public operator fun Long.times(other: UShort): Long = times(other.toInt())
public operator fun Int.times(other: UShort): Int = times(other.toInt())
public operator fun Short.times(other: UShort): Int = times(other.toInt())
public operator fun Byte.times(other: UShort): Int = times(other.toInt())

public operator fun Long.div(other: UShort): Long = div(other.toInt())
public operator fun Int.div(other: UShort): Int = div(other.toInt())
public operator fun Short.div(other: UShort): Int = div(other.toInt())
public operator fun Byte.div(other: UShort): Int = div(other.toInt())

public operator fun Long.rem(other: UShort): Long = rem(other.toInt())
public operator fun Int.rem(other: UShort): Int = rem(other.toInt())
public operator fun Short.rem(other: UShort): Int = rem(other.toInt())
public operator fun Byte.rem(other: UShort): Int = rem(other.toInt())

/** UByte -> Signed */

public operator fun UByte.plus(other: Long): ULong = if (other > 0) plus(other.toULong()) else minus(abs(other).toULong())
public operator fun UByte.plus(other: Int): UInt = if (other > 0) plus(other.toUInt()) else minus(abs(other).toUInt())
public operator fun UByte.plus(other: Short): UInt = plus(other.toInt())
public operator fun UByte.plus(other: Byte): UInt = plus(other.toShort())

public operator fun UByte.minus(other: Long): ULong = if (other > 0) minus(other.toULong()) else plus(abs(other).toULong())
public operator fun UByte.minus(other: Int): UInt = if (other > 0) minus(other.toUInt()) else plus(abs(other).toUInt())
public operator fun UByte.minus(other: Short): UInt = minus(other.toInt())
public operator fun UByte.minus(other: Byte): UInt = minus(other.toInt())

public operator fun UByte.times(other: Long): ULong = times(other.toULong())
public operator fun UByte.times(other: Int): ULong = times(other.toULong())
public operator fun UByte.times(other: Short): UInt = times(other.toUInt())
public operator fun UByte.times(other: Byte): UInt = times(other.toUInt())

public operator fun UByte.div(other: Long): ULong = div(other.toULong())
public operator fun UByte.div(other: Int): ULong = div(other.toULong())
public operator fun UByte.div(other: Short): UInt = div(other.toUInt())
public operator fun UByte.div(other: Byte): UInt = div(other.toUInt())

public operator fun UByte.rem(other: Long): UInt = rem(other.toUInt())
public operator fun UByte.rem(other: Int): UInt = rem(other.toUInt())
public operator fun UByte.rem(other: Short): UInt = rem(other.toUInt())
public operator fun UByte.rem(other: Byte): UInt = rem(other.toUInt())

/** Signed -> UByte */

public operator fun Long.plus(other: UByte): Long = plus(other.toInt())
public operator fun Int.plus(other: UByte): Int = plus(other.toInt())
public operator fun Short.plus(other: UByte): Int = plus(other.toInt())
public operator fun Byte.plus(other: UByte): Int = plus(other.toInt())

public operator fun Long.minus(other: UByte): Long = minus(other.toInt())
public operator fun Int.minus(other: UByte): Int = minus(other.toInt())
public operator fun Short.minus(other: UByte): Int = minus(other.toInt())
public operator fun Byte.minus(other: UByte): Int = minus(other.toInt())

public operator fun Long.times(other: UByte): Long = times(other.toInt())
public operator fun Int.times(other: UByte): Int = times(other.toInt())
public operator fun Short.times(other: UByte): Int = times(other.toInt())
public operator fun Byte.times(other: UByte): Int = times(other.toInt())

public operator fun Long.div(other: UByte): Long = div(other.toInt())
public operator fun Int.div(other: UByte): Int = div(other.toInt())
public operator fun Short.div(other: UByte): Int = div(other.toInt())
public operator fun Byte.div(other: UByte): Int = div(other.toInt())

public operator fun Long.rem(other: UByte): Long = rem(other.toInt())
public operator fun Int.rem(other: UByte): Int = rem(other.toInt())
public operator fun Short.rem(other: UByte): Int = rem(other.toInt())
public operator fun Byte.rem(other: UByte): Int = rem(other.toInt())