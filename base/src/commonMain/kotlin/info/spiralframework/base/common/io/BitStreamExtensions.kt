@file:Suppress("unused")

package info.spiralframework.base.common.io

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.decodeToString

@ExperimentalUnsignedTypes
fun InputFlow.readInt64LE(): Long? {
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
fun InputFlow.readInt64BE(): Long? {
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
fun InputFlow.readUInt64LE(): ULong? {
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
fun InputFlow.readUInt64BE(): ULong? {
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
fun InputFlow.readInt32LE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun InputFlow.readInt32BE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

@ExperimentalUnsignedTypes
fun InputFlow.readUInt32LE(): UInt? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return ((d shl 24) or (c shl 16) or (b shl 8) or a).toUInt()
}

@ExperimentalUnsignedTypes
fun InputFlow.readUInt32BE(): UInt? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return ((a shl 24) or (b shl 16) or (c shl 8) or d).toUInt()
}

@ExperimentalUnsignedTypes
fun InputFlow.readInt16LE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun InputFlow.readInt16BE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return (a shl 8) or b
}

@ExperimentalUnsignedTypes
fun InputFlow.readFloatBE(): Float? = this.readInt32BE()?.let { Float.fromBits(it) }
@ExperimentalUnsignedTypes
fun InputFlow.readFloatLE(): Float? = this.readInt32LE()?.let { Float.fromBits(it) }
@ExperimentalUnsignedTypes
fun InputFlow.readFloat32BE(): Float? = this.readInt32BE()?.let { Float.fromBits(it) }
@ExperimentalUnsignedTypes
fun InputFlow.readFloat32LE(): Float? = this.readInt32LE()?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
fun InputFlow.readDoubleBE(): Double? = this.readInt64BE()?.let { Double.fromBits(it) }
@ExperimentalUnsignedTypes
fun InputFlow.readDoubleLE(): Double? = this.readInt64LE()?.let { Double.fromBits(it) }
@ExperimentalUnsignedTypes
fun InputFlow.readFloat64BE(): Double? = this.readInt64BE()?.let { Double.fromBits(it) }
@ExperimentalUnsignedTypes
fun InputFlow.readFloat64LE(): Double? = this.readInt64LE()?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
fun <T> InputFlow.read(serialise: (InputFlow) -> T?): T? = serialise(this)
//fun <T> CountingInputFlow.readSource(source: () -> InputFlow, serialise: (() -> InputFlow) -> T?): T? = serialise(source.from(streamOffset))

@ExperimentalUnsignedTypes
fun OutputFlow.writeFloatBE(float: Float) = writeInt32BE(float.toRawBits())
@ExperimentalUnsignedTypes
fun OutputFlow.writeFloatLE(float: Float) = writeInt32LE(float.toRawBits())

@ExperimentalUnsignedTypes
fun OutputFlow.writeInt64LE(num: Number) {
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
fun OutputFlow.writeInt64BE(num: Number) {
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
fun OutputFlow.writeInt32LE(num: Number) {
    val int = num.toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 24) and 0xFF)
}

@ExperimentalUnsignedTypes
fun OutputFlow.writeInt32BE(num: Number) {
    val int = num.toInt()

    write((int shr 24) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

@ExperimentalUnsignedTypes
fun OutputFlow.writeUInt32BE(num: Number) {
    val int = num.toInt()

    write((int ushr 24) and 0xFF)
    write((int ushr 16) and 0xFF)
    write((int ushr 8) and 0xFF)
    write(int and 0xFF)
}

@ExperimentalUnsignedTypes
fun OutputFlow.writeInt16LE(num: Number) {
    val int = num.toShort().toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
}

@ExperimentalUnsignedTypes
fun OutputFlow.writeInt16BE(num: Number) {
    val int = num.toShort().toInt()

    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

/**
 * Only supports 1, 2, 4, or 8
 */
@ExperimentalUnsignedTypes
inline fun InputFlow.readIntXLE(x: Int): Number? =
        when (x) {
            1 -> read()
            2 -> readInt16LE()
            4 -> readInt32LE()
            8 -> readInt64LE()
            else -> throw IllegalArgumentException("$x is not 1, 2, 4, or 8")
        }

@ExperimentalUnsignedTypes
inline fun OutputFlow.writeIntXLE(num: Number, x: Int) =
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
fun InputFlow.readString(len: Int, encoding: TextCharsets, overrideMaxLen: Boolean = false): String {
    val data =
        ByteArray(len.coerceAtLeast(0).run { if (!overrideMaxLen) this.coerceAtMost(1024 * 1024) else this })
    read(data)
    return data.decodeToString(encoding)
}

@ExperimentalUnsignedTypes
fun InputFlow.readXBytes(x: Int): ByteArray = ByteArray(x).apply { this@readXBytes.read(this) }

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
fun InputFlow.readNullTerminatedUTF8String(): String = readNullTerminatedString(encoding = TextCharsets.UTF_8)
@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
fun InputFlow.readNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryOutputFlow()
    
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
fun InputFlow.readSingleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryOutputFlow()

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
fun InputFlow.readDoubleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
    val data = BinaryOutputFlow()

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
public fun InputFlow.copyToOutputFlow(output: OutputFlow): Long = copyTo(output)
@ExperimentalUnsignedTypes
public fun InputFlow.copyTo(output: OutputFlow, bufferSize: Int = 8192): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes != null) {
        output.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}

@ExperimentalUnsignedTypes
public fun OutputFlow.copyFrom(input: InputFlow): Long = input.copyTo(this)
