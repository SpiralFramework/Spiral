@file:Suppress("unused")

package info.spiralframework.base.common.io

import info.spiralframework.base.binding.BinaryDataSink
import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.decodeToString

@ExperimentalUnsignedTypes
fun DataStream.readInt64LE(): Long? {
    val a = read()?.toLong() ?: return null
    val b = read()?.toLong() ?: return null
    val c = read()?.toLong() ?: return null
    val d = read()?.toLong() ?: return null
    val e = read()?.toLong() ?: return null
    val f = read()?.toLong() ?: return null
    val g = read()?.toLong() ?: return null
    val h = read()?.toLong() ?: return null

    return (h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun DataStream.readInt64BE(): Long? {
    val a = read()?.toLong() ?: return null
    val b = read()?.toLong() ?: return null
    val c = read()?.toLong() ?: return null
    val d = read()?.toLong() ?: return null
    val e = read()?.toLong() ?: return null
    val f = read()?.toLong() ?: return null
    val g = read()?.toLong() ?: return null
    val h = read()?.toLong() ?: return null

    return (a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h
}

@ExperimentalUnsignedTypes
fun DataStream.readUInt64LE(): ULong? {
    val a = read()?.toLong() ?: return null
    val b = read()?.toLong() ?: return null
    val c = read()?.toLong() ?: return null
    val d = read()?.toLong() ?: return null
    val e = read()?.toLong() ?: return null
    val f = read()?.toLong() ?: return null
    val g = read()?.toLong() ?: return null
    val h = read()?.toLong() ?: return null

    return ((h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a).toULong()
}

@ExperimentalUnsignedTypes
fun DataStream.readUInt64BE(): ULong? {
    val a = read()?.toLong() ?: return null
    val b = read()?.toLong() ?: return null
    val c = read()?.toLong() ?: return null
    val d = read()?.toLong() ?: return null
    val e = read()?.toLong() ?: return null
    val f = read()?.toLong() ?: return null
    val g = read()?.toLong() ?: return null
    val h = read()?.toLong() ?: return null

    return ((a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h).toULong()
}

@ExperimentalUnsignedTypes
fun DataStream.readInt32LE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun DataStream.readInt32BE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

@ExperimentalUnsignedTypes
fun DataStream.readUInt32LE(): UInt? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return ((d shl 24) or (c shl 16) or (b shl 8) or a).toUInt()
}

@ExperimentalUnsignedTypes
fun DataStream.readUInt32BE(): UInt? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return ((a shl 24) or (b shl 16) or (c shl 8) or d).toUInt()
}

@ExperimentalUnsignedTypes
fun DataStream.readInt16LE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun DataStream.readInt16BE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return (a shl 8) or b
}

@ExperimentalUnsignedTypes
fun DataStream.readFloatBE(): Float? = this.readInt32BE()?.let { Float.fromBits(it) }
@ExperimentalUnsignedTypes
fun DataStream.readFloatLE(): Float? = this.readInt32LE()?.let { Float.fromBits(it) }
@ExperimentalUnsignedTypes
fun DataStream.readFloat32BE(): Float? = this.readInt32BE()?.let { Float.fromBits(it) }
@ExperimentalUnsignedTypes
fun DataStream.readFloat32LE(): Float? = this.readInt32LE()?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
fun DataStream.readDoubleBE(): Double? = this.readInt64BE()?.let { Double.fromBits(it) }
@ExperimentalUnsignedTypes
fun DataStream.readDoubleLE(): Double? = this.readInt64LE()?.let { Double.fromBits(it) }
@ExperimentalUnsignedTypes
fun DataStream.readFloat64BE(): Double? = this.readInt64BE()?.let { Double.fromBits(it) }
@ExperimentalUnsignedTypes
fun DataStream.readFloat64LE(): Double? = this.readInt64LE()?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
fun <T> DataStream.read(serialise: (DataStream) -> T?): T? = serialise(this)
//fun <T> CountingDataStream.readSource(source: () -> DataStream, serialise: (() -> DataStream) -> T?): T? = serialise(source.from(streamOffset))

@ExperimentalUnsignedTypes
fun DataSink.writeFloatBE(float: Float) = writeInt32BE(float.toRawBits())
@ExperimentalUnsignedTypes
fun DataSink.writeFloatLE(float: Float) = writeInt32LE(float.toRawBits())

@ExperimentalUnsignedTypes
fun DataSink.writeInt64LE(num: Number) {
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

@ExperimentalUnsignedTypes
fun DataSink.writeInt64BE(num: Number) {
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

@ExperimentalUnsignedTypes
fun DataSink.writeInt32LE(num: Number) {
    val int = num.toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 24) and 0xFF)
}

@ExperimentalUnsignedTypes
fun DataSink.writeInt32BE(num: Number) {
    val int = num.toInt()

    write((int shr 24) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

@ExperimentalUnsignedTypes
fun DataSink.writeUInt32BE(num: Number) {
    val int = num.toInt()

    write((int ushr 24) and 0xFF)
    write((int ushr 16) and 0xFF)
    write((int ushr 8) and 0xFF)
    write(int and 0xFF)
}

@ExperimentalUnsignedTypes
fun DataSink.writeInt16LE(num: Number) {
    val int = num.toShort().toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
}

@ExperimentalUnsignedTypes
fun DataSink.writeInt16BE(num: Number) {
    val int = num.toShort().toInt()

    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

/**
 * Only supports 1, 2, 4, or 8
 */
@ExperimentalUnsignedTypes
inline fun DataStream.readIntXLE(x: Int): Number? =
        when (x) {
            1 -> read()
            2 -> readInt16LE()
            4 -> readInt32LE()
            8 -> readInt64LE()
            else -> throw IllegalArgumentException("$x is not 1, 2, 4, or 8")
        }

@ExperimentalUnsignedTypes
inline fun DataSink.writeIntXLE(num: Number, x: Int) =
        when (x) {
            1 -> write(num.toInt())
            2 -> writeInt16LE(num)
            4 -> writeInt32LE(num)
            8 -> writeInt64LE(num)
            else -> throw IllegalArgumentException("$x is not 1, 2, 4, or 8")
        }

fun makeMask(vararg bits: Int): Int {
    var mask = 0
    for (bit in bits)
        mask = mask or ((1 shl (bit + 1)) - 1)

    return mask
}

fun Number.toInt16LE(): IntArray {
    val num = toInt() and 0xFFFF
    return intArrayOf(num shr 8, num and 0xFF)
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
fun DataStream.readString(len: Int, encoding: TextCharsets, overrideMaxLen: Boolean = false): String {
    val data =
        ByteArray(len.coerceAtLeast(0).run { if (!overrideMaxLen) this.coerceAtMost(1024 * 1024) else this })
    read(data)
    return data.decodeToString(encoding)
}

@ExperimentalUnsignedTypes
fun DataStream.readXBytes(x: Int): ByteArray = ByteArray(x).apply { this@readXBytes.read(this) }

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
fun DataStream.readNullTerminatedUTF8String(): String = readNullTerminatedString(encoding = TextCharsets.UTF_8)
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
fun DataStream.readNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryDataSink()
    
    while (true) {
        val read = readIntXLE(encoding.bytesForNull) ?: break //This **should** work
        require(read != -1) { "Uho..., it's -1 somehow" }
        if (read == 0x00)
            break

        data.writeIntXLE(read, encoding.bytesForNull)
    }

    return data.getData().decodeToString(encoding)
}
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
fun DataStream.readSingleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryDataSink()

    while (true) {
        val read = read() ?: break
        require(read != -1) { "Uho..., it's -1 somehow" }
        if (read == 0x00)
            break

        data.write(read)
    }

    return data.getData().decodeToString(encoding)
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
fun DataStream.readDoubleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryDataSink()

    while (true) {
        val read = readInt16LE() ?: break
        require(read != -1) { "Uho..., it's -1 somehow" }
        if (read == 0x00)
            break

        data.writeInt16LE(read)
    }

    return data.getData().decodeToString(encoding)
}

@ExperimentalUnsignedTypes
public fun DataStream.copyToSink(sink: DataSink): Long = copyTo(sink)
@ExperimentalUnsignedTypes
public fun DataStream.copyTo(sink: DataSink, bufferSize: Int = 8192): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes != null) {
        sink.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}

@ExperimentalUnsignedTypes
public fun DataSink.copyFrom(stream: DataStream): Long = stream.copyTo(this)
