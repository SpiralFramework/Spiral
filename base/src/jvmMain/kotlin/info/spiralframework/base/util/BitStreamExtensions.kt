package info.spiralframework.base.util

import info.spiralframework.base.CountingInputStream
import java.io.InputStream
import java.io.OutputStream

@Deprecated("Use InputFlow instead")
fun InputStream.readInt64LE(): Long {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()
    val e = read().toLong()
    val f = read().toLong()
    val g = read().toLong()
    val h = read().toLong()

    return (h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a
}

@Deprecated("Use InputFlow instead")
fun InputStream.readInt64BE(): Long {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()
    val e = read().toLong()
    val f = read().toLong()
    val g = read().toLong()
    val h = read().toLong()

    return (a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h
}

@Deprecated("Use InputFlow instead")
fun InputStream.readUInt64LE(): ULong {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()
    val e = read().toLong()
    val f = read().toLong()
    val g = read().toLong()
    val h = read().toLong()

    return ((h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a).toULong()
}

@Deprecated("Use InputFlow instead")
fun InputStream.readUInt64BE(): ULong {
    val a = read().toLong()
    val b = read().toLong()
    val c = read().toLong()
    val d = read().toLong()
    val e = read().toLong()
    val f = read().toLong()
    val g = read().toLong()
    val h = read().toLong()

    return ((a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h).toULong()
}

@Deprecated("Use InputFlow instead")
fun InputStream.readInt32LE(): Int {
    val a = read()
    val b = read()
    val c = read()
    val d = read()

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

@Deprecated("Use InputFlow instead")
fun InputStream.readInt32BE(): Int {
    val a = read()
    val b = read()
    val c = read()
    val d = read()

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

@Deprecated("Use InputFlow instead")
fun InputStream.readUInt32LE(): UInt {
    val a = read()
    val b = read()
    val c = read()
    val d = read()

    return ((d shl 24) or (c shl 16) or (b shl 8) or a).toUInt()
}

@Deprecated("Use InputFlow instead")
fun InputStream.readUInt32BE(): UInt {
    val a = read()
    val b = read()
    val c = read()
    val d = read()

    return ((a shl 24) or (b shl 16) or (c shl 8) or d).toUInt()
}

@Deprecated("Use InputFlow instead")
fun InputStream.readInt16LE(): Int {
    val a = read()
    val b = read()

    return (b shl 8) or a
}

@Deprecated("Use InputFlow instead")
fun InputStream.readInt16BE(): Int {
    val a = read()
    val b = read()

    return (a shl 8) or b
}

@Deprecated("Use InputFlow instead")
fun InputStream.readFloatBE(): Float = java.lang.Float.intBitsToFloat(this.readInt32BE())
@Deprecated("Use InputFlow instead")
fun InputStream.readFloatLE(): Float = java.lang.Float.intBitsToFloat(this.readInt32LE())
@Deprecated("Use InputFlow instead")
fun InputStream.readFloat32BE(): Float = java.lang.Float.intBitsToFloat(this.readInt32BE())
@Deprecated("Use InputFlow instead")
fun InputStream.readFloat32LE(): Float = java.lang.Float.intBitsToFloat(this.readInt32LE())

@Deprecated("Use InputFlow instead")
fun InputStream.readDoubleBE(): Double = java.lang.Double.longBitsToDouble(this.readInt64BE())
@Deprecated("Use InputFlow instead")
fun InputStream.readDoubleLE(): Double = java.lang.Double.longBitsToDouble(this.readInt64LE())
@Deprecated("Use InputFlow instead")
fun InputStream.readFloat64BE(): Double = java.lang.Double.longBitsToDouble(this.readInt64BE())
@Deprecated("Use InputFlow instead")
fun InputStream.readFloat64LE(): Double = java.lang.Double.longBitsToDouble(this.readInt64LE())


@Deprecated("Use InputFlow instead")
fun <T> InputStream.read(serialise: (InputStream) -> T?): T? = serialise(this)
@Deprecated("Use InputFlow instead")
fun <T> CountingInputStream.readSource(source: () -> InputStream, serialise: (() -> InputStream) -> T?): T? = serialise(source.from(streamOffset))

@Deprecated("Use InputFlow instead")
fun OutputStream.writeFloatBE(float: Float) = writeInt32BE(java.lang.Float.floatToIntBits(float))
@Deprecated("Use InputFlow instead")
fun OutputStream.writeFloatLE(float: Float) = writeInt32LE(java.lang.Float.floatToIntBits(float))

@Deprecated("Use InputFlow instead")
fun OutputStream.writeInt64LE(num: Number) {
    val long = num.toLong()

    write(long.toInt() and 0xFF)
    write((long shr 8).toInt() and 0xFF)
    write((long shr 16).toInt() and 0xFF)
    write((long shr 24).toInt() and 0xFF)
    write((long shr 32).toInt() and 0xFF)
    write((long shr 40).toInt() and 0xFF)
    write((long shr 48).toInt() and 0xFF)
    write((long shr 56).toInt() and 0xFF)
}

@Deprecated("Use InputFlow instead")
fun OutputStream.writeInt64BE(num: Number) {
    val long = num.toLong()

    write((long shr 56).toInt() and 0xFF)
    write((long shr 48).toInt() and 0xFF)
    write((long shr 40).toInt() and 0xFF)
    write((long shr 32).toInt() and 0xFF)
    write((long shr 24).toInt() and 0xFF)
    write((long shr 16).toInt() and 0xFF)
    write((long shr 8).toInt() and 0xFF)
    write(long.toInt() and 0xFF)
}

@Deprecated("Use InputFlow instead")
fun OutputStream.writeInt32LE(num: Number) {
    val int = num.toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 24) and 0xFF)
}

@Deprecated("Use InputFlow instead")
fun OutputStream.writeInt32BE(num: Number) {
    val int = num.toInt()

    write((int shr 24) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

@Deprecated("Use InputFlow instead")
fun OutputStream.writeUInt32BE(num: Number) {
    val int = num.toInt()

    write((int ushr 24) and 0xFF)
    write((int ushr 16) and 0xFF)
    write((int ushr 8) and 0xFF)
    write(int and 0xFF)
}

@Deprecated("Use InputFlow instead")
fun OutputStream.writeInt16LE(num: Number) {
    val int = num.toShort().toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
}

@Deprecated("Use InputFlow instead")
fun OutputStream.writeInt16BE(num: Number) {
    val int = num.toShort().toInt()

    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

/**
 * Only supports 1, 2, 4, or 8
 */
inline @Deprecated("Use InputFlow instead")
fun InputStream.readIntXLE(x: Int): Number =
        when (x) {
            1 -> read()
            2 -> readInt16LE()
            4 -> readInt32LE()
            8 -> readInt64LE()
            else -> throw IllegalArgumentException("$x is not 1, 2, 4, or 8")
        }

inline @Deprecated("Use InputFlow instead")
fun OutputStream.writeIntXLE(num: Number, x: Int) =
        when (x) {
            1 -> write(num.toInt())
            2 -> writeInt16LE(num)
            4 -> writeInt32LE(num)
            8 -> writeInt64LE(num)
            else -> throw IllegalArgumentException("$x is not 1, 2, 4, or 8")
        }

@Deprecated("Use InputFlow instead")
fun makeMask(vararg bits: Int): Int {
    var mask = 0
    for (bit in bits)
        mask = mask or ((1 shl (bit + 1)) - 1)

    return mask
}

@Deprecated("Use InputFlow instead")
fun Number.toInt16LE(): IntArray {
    val num = toInt() and 0xFFFF
    return intArrayOf(num shr 8, num and 0xFF)
}