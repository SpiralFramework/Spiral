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
operator fun UInt.plus(other: Short): UInt = plus(other.toLong())
@ExperimentalUnsignedTypes
operator fun UInt.plus(other: Byte): UInt = plus(other.toLong())

@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Long): UInt = if (other > 0) minus(other.toUInt()) else plus(abs(other).toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Int): UInt = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Short): UInt = minus(other.toLong())
@ExperimentalUnsignedTypes
operator fun UInt.minus(other: Byte): UInt = minus(other.toLong())

@ExperimentalUnsignedTypes
operator fun UInt.times(other: Long): UInt = times(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.times(other: Int): UInt = times(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.times(other: Short): UInt = times(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.times(other: Byte): UInt = times(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UInt.div(other: Long): UInt = div(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.div(other: Int): UInt = div(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.div(other: Short): UInt = div(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.div(other: Byte): UInt = div(other.toUInt())

@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Long): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Int): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Short): UInt = rem(other.toUInt())
@ExperimentalUnsignedTypes
operator fun UInt.rem(other: Byte): UInt = rem(other.toUInt())