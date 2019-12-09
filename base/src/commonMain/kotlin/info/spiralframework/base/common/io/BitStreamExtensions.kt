@file:Suppress("unused")

package info.spiralframework.base.common.io

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.binding.TextCharsets
import info.spiralframework.base.binding.decodeToString
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.base.common.io.flow.PeekableInputFlow

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekInt64LE(): Long? {
    val a = peek(1)?.toLong() ?: return null
    val b = peek(2)?.toLong() ?: return null
    val c = peek(3)?.toLong() ?: return null
    val d = peek(4)?.toLong() ?: return null
    val e = peek(5)?.toLong() ?: return null
    val f = peek(6)?.toLong() ?: return null
    val g = peek(7)?.toLong() ?: return null
    val h = peek(8)?.toLong() ?: return null

    return (h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekInt64BE(): Long? {
    val a = peek(1)?.toLong() ?: return null
    val b = peek(2)?.toLong() ?: return null
    val c = peek(3)?.toLong() ?: return null
    val d = peek(4)?.toLong() ?: return null
    val e = peek(5)?.toLong() ?: return null
    val f = peek(6)?.toLong() ?: return null
    val g = peek(7)?.toLong() ?: return null
    val h = peek(8)?.toLong() ?: return null

    return (a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekUInt64LE(): ULong? {
    val a = peek(1)?.toLong() ?: return null
    val b = peek(2)?.toLong() ?: return null
    val c = peek(3)?.toLong() ?: return null
    val d = peek(4)?.toLong() ?: return null
    val e = peek(5)?.toLong() ?: return null
    val f = peek(6)?.toLong() ?: return null
    val g = peek(7)?.toLong() ?: return null
    val h = peek(8)?.toLong() ?: return null

    return ((h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a).toULong()
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekUInt64BE(): ULong? {
    val a = peek(1)?.toLong() ?: return null
    val b = peek(2)?.toLong() ?: return null
    val c = peek(3)?.toLong() ?: return null
    val d = peek(4)?.toLong() ?: return null
    val e = peek(5)?.toLong() ?: return null
    val f = peek(6)?.toLong() ?: return null
    val g = peek(7)?.toLong() ?: return null
    val h = peek(8)?.toLong() ?: return null

    return ((a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h).toULong()
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekInt32LE(): Int? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null
    val c = peek(3) ?: return null
    val d = peek(4) ?: return null

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekInt32BE(): Int? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null
    val c = peek(3) ?: return null
    val d = peek(4) ?: return null

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekUInt32LE(): UInt? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null
    val c = peek(3) ?: return null
    val d = peek(4) ?: return null

    return ((d shl 24) or (c shl 16) or (b shl 8) or a).toUInt()
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekUInt32BE(): UInt? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null
    val c = peek(3) ?: return null
    val d = peek(4) ?: return null

    return ((a shl 24) or (b shl 16) or (c shl 8) or d).toUInt()
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekInt16LE(): Int? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null

    return (b shl 8) or a
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekInt16BE(): Int? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null

    return (a shl 8) or b
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekUInt16LE(): Int? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null

    return ((b shl 8) or a) and 0xFFFF
}

@ExperimentalUnsignedTypes
suspend fun PeekableInputFlow.peekUInt16BE(): Int? {
    val a = peek(1) ?: return null
    val b = peek(2) ?: return null

    return ((a shl 8) or b) and 0xFFFF
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readInt64LE(): Long? {
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
suspend fun InputFlow.readInt64BE(): Long? {
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
suspend fun InputFlow.readUInt64LE(): ULong? {
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
suspend fun InputFlow.readUInt64BE(): ULong? {
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
suspend fun InputFlow.readInt32LE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readInt32BE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readUInt32LE(): UInt? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return ((d shl 24) or (c shl 16) or (b shl 8) or a).toUInt()
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readUInt32BE(): UInt? {
    val a = read() ?: return null
    val b = read() ?: return null
    val c = read() ?: return null
    val d = read() ?: return null

    return ((a shl 24) or (b shl 16) or (c shl 8) or d).toUInt()
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readInt16LE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return (b shl 8) or a
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readInt16BE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return (a shl 8) or b
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readUInt16LE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return ((b shl 8) or a) and 0xFFFF
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readUInt16BE(): Int? {
    val a = read() ?: return null
    val b = read() ?: return null

    return ((a shl 8) or b) and 0xFFFF
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readFloatBE(): Float? = this.readInt32BE()?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
suspend fun InputFlow.readFloatLE(): Float? = this.readInt32LE()?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
suspend fun InputFlow.readFloat32BE(): Float? = this.readInt32BE()?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
suspend fun InputFlow.readFloat32LE(): Float? = this.readInt32LE()?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
suspend fun InputFlow.readDoubleBE(): Double? = this.readInt64BE()?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
suspend fun InputFlow.readDoubleLE(): Double? = this.readInt64LE()?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
suspend fun InputFlow.readFloat64BE(): Double? = this.readInt64BE()?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
suspend fun InputFlow.readFloat64LE(): Double? = this.readInt64LE()?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readInt64LE(index: Int): Long? {
    if (size - 8 < index)
        return null

    val a = this[index].toLong() and 0xFF
    val b = this[index + 1].toLong() and 0xFF
    val c = this[index + 2].toLong() and 0xFF
    val d = this[index + 3].toLong() and 0xFF
    val e = this[index + 4].toLong() and 0xFF
    val f = this[index + 5].toLong() and 0xFF
    val g = this[index + 6].toLong() and 0xFF
    val h = this[index + 7].toLong() and 0xFF

    return (h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun ByteArray.readInt64BE(index: Int): Long? {
    if (size - 8 < index)
        return null

    val a = this[index].toLong() and 0xFF
    val b = this[index + 1].toLong() and 0xFF
    val c = this[index + 2].toLong() and 0xFF
    val d = this[index + 3].toLong() and 0xFF
    val e = this[index + 4].toLong() and 0xFF
    val f = this[index + 5].toLong() and 0xFF
    val g = this[index + 6].toLong() and 0xFF
    val h = this[index + 7].toLong() and 0xFF

    return (a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h
}

@ExperimentalUnsignedTypes
fun ByteArray.readUInt64LE(index: Int): ULong? {
    if (size - 8 < index)
        return null

    val a = this[index].toULong() and 0xFFu
    val b = this[index + 1].toULong() and 0xFFu
    val c = this[index + 2].toULong() and 0xFFu
    val d = this[index + 3].toULong() and 0xFFu
    val e = this[index + 4].toULong() and 0xFFu
    val f = this[index + 5].toULong() and 0xFFu
    val g = this[index + 6].toULong() and 0xFFu
    val h = this[index + 7].toULong() and 0xFFu

    return (h shl 56) or (g shl 48) or (f shl 40) or (e shl 32) or
            (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun ByteArray.readUInt64BE(index: Int): ULong? {
    if (size - 8 < index)
        return null

    val a = this[index].toULong() and 0xFFu
    val b = this[index + 1].toULong() and 0xFFu
    val c = this[index + 2].toULong() and 0xFFu
    val d = this[index + 3].toULong() and 0xFFu
    val e = this[index + 4].toULong() and 0xFFu
    val f = this[index + 5].toULong() and 0xFFu
    val g = this[index + 6].toULong() and 0xFFu
    val h = this[index + 7].toULong() and 0xFFu

    return (a shl 56) or (b shl 48) or (c shl 40) or (d shl 32) or
            (e shl 24) or (f shl 16) or (g shl 8) or h
}

@ExperimentalUnsignedTypes
fun ByteArray.readInt32LE(index: Int): Int? {
    if (size - 4 < index)
        return null

    val a = this[index].toInt() and 0xFF
    val b = this[index + 1].toInt() and 0xFF
    val c = this[index + 2].toInt() and 0xFF
    val d = this[index + 3].toInt() and 0xFF

    return (d shl 24) or (c shl 16) or (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun ByteArray.readInt32BE(index: Int): Int? {
    if (size - 4 < index)
        return null

    val a = this[index].toInt() and 0xFF
    val b = this[index + 1].toInt() and 0xFF
    val c = this[index + 2].toInt() and 0xFF
    val d = this[index + 3].toInt() and 0xFF

    return (a shl 24) or (b shl 16) or (c shl 8) or d
}

@ExperimentalUnsignedTypes
fun ByteArray.readUInt32LE(index: Int): UInt? {
    if (size - 4 < index)
        return null

    val a = this[index].toUInt() and 0xFFu
    val b = this[index + 1].toUInt() and 0xFFu
    val c = this[index + 2].toUInt() and 0xFFu
    val d = this[index + 3].toUInt() and 0xFFu

    return ((d shl 24) or (c shl 16) or (b shl 8) or a)
}

@ExperimentalUnsignedTypes
fun ByteArray.readUInt32BE(index: Int): UInt? {
    if (size - 4 < index)
        return null

    val a = this[index].toUInt() and 0xFFu
    val b = this[index + 1].toUInt() and 0xFFu
    val c = this[index + 2].toUInt() and 0xFFu
    val d = this[index + 3].toUInt() and 0xFFu

    return ((a shl 24) or (b shl 16) or (c shl 8) or d).toUInt()
}

@ExperimentalUnsignedTypes
fun ByteArray.readInt24BE(index: Int): Int? {
    if (size - 3 < index)
        return null

    val a = this[index].toInt() and 0xFF
    val b = this[index + 1].toInt() and 0xFF
    val c = this[index + 2].toInt() and 0xFF

    return (a shl 16) or (b shl 8) or c
}

@ExperimentalUnsignedTypes
fun ByteArray.readInt16LE(index: Int): Int? {
    if (size - 2 < index)
        return null

    val a = this[index].toInt() and 0xFF
    val b = this[index + 1].toInt() and 0xFF

    return (b shl 8) or a
}

@ExperimentalUnsignedTypes
fun ByteArray.readInt16BE(index: Int): Int? {
    if (size - 2 < index)
        return null

    val a = this[index].toInt() and 0xFF
    val b = this[index + 1].toInt() and 0xFF

    return (a shl 8) or b
}

@ExperimentalUnsignedTypes
fun ByteArray.readFloatBE(index: Int): Float? = this.readInt32BE(index)?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readFloatLE(index: Int): Float? = this.readInt32LE(index)?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readFloat32BE(index: Int): Float? = this.readInt32BE(index)?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readFloat32LE(index: Int): Float? = this.readInt32LE(index)?.let { Float.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readDoubleBE(index: Int): Double? = this.readInt64BE(index)?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readDoubleLE(index: Int): Double? = this.readInt64LE(index)?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readFloat64BE(index: Int): Double? = this.readInt64BE(index)?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
fun ByteArray.readFloat64LE(index: Int): Double? = this.readInt64LE(index)?.let { Double.fromBits(it) }

@ExperimentalUnsignedTypes
fun <T> InputFlow.read(serialise: (InputFlow) -> T?): T? = serialise(this)
//fun <T> CountingInputFlow.readSource(source: () -> InputFlow, serialise: (() -> InputFlow) -> T?): T? = serialise(source.from(streamOffset))

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeFloatBE(float: Float) = writeInt32BE(float.toRawBits())

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeFloatLE(float: Float) = writeInt32LE(float.toRawBits())

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeInt64LE(num: Number) {
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
suspend fun OutputFlow.writeInt64BE(num: Number) {
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
suspend fun OutputFlow.writeInt32LE(num: Number) {
    val int = num.toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 24) and 0xFF)
}

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeInt32BE(num: Number) {
    val int = num.toInt()

    write((int shr 24) and 0xFF)
    write((int shr 16) and 0xFF)
    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeUInt32BE(num: Number) {
    val int = num.toInt()

    write((int ushr 24) and 0xFF)
    write((int ushr 16) and 0xFF)
    write((int ushr 8) and 0xFF)
    write(int and 0xFF)
}

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeInt16LE(num: Number) {
    val int = num.toShort().toInt()

    write(int and 0xFF)
    write((int shr 8) and 0xFF)
}

@ExperimentalUnsignedTypes
suspend fun OutputFlow.writeInt16BE(num: Number) {
    val int = num.toShort().toInt()

    write((int shr 8) and 0xFF)
    write(int and 0xFF)
}

@ExperimentalUnsignedTypes
suspend inline fun InputFlow.readIntXLE(x: Int): Number? {
    when (x) {
        1 -> return read()
        2 -> return readInt16LE()
        4 -> return readInt32LE()
        8 -> return readInt64LE()
        else -> {
            var num: Long = 0
            val maxPos = x.coerceIn(1..8)
            for (i in 0 until maxPos) {
                num = num or ((read()?.toLong() ?: return null) shl (i * 8))
            }
            return num
        }
    }
}

@ExperimentalUnsignedTypes
suspend inline fun OutputFlow.writeIntXLE(num: Number, x: Int) =
        when (x) {
            1 -> write(num.toInt())
            2 -> writeInt16LE(num)
            4 -> writeInt32LE(num)
            8 -> writeInt64LE(num)
            else -> {
                val long = num.toLong()
                val maxPos = x.coerceIn(1..8)
                for (i in 0 until maxPos) {
                    write(((long shr (i * 8)) and 0xFF).toInt())
                }
            }
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
suspend fun InputFlow.readString(len: Int, encoding: TextCharsets, overrideMaxLen: Boolean = false): String {
    val data = ByteArray(if (overrideMaxLen) len.coerceAtLeast(0) else len.coerceIn(0, 1024 * 1024))
    read(data)
    return data.decodeToString(encoding)
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readAsciiString(len: Int, overrideMaxLen: Boolean = false): String? {
    val data = ByteArray(if (overrideMaxLen) len.coerceAtLeast(0) else len.coerceIn(0, 1024 * 1024))
    if (read(data) != data.size) return null
    return String(CharArray(data.size) { data[it].toChar() })
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readNumBytes(num: Int): ByteArray {
    val data = ByteArray(num)
    read(data)
    return data
}

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun InputFlow.readNullTerminatedUTF8String(): String = readNullTerminatedString(encoding = TextCharsets.UTF_8)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun InputFlow.readNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
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
suspend fun InputFlow.readSingleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
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
suspend fun InputFlow.readDoubleByteNullTerminatedString(maxLen: Int = 255, encoding: TextCharsets = TextCharsets.UTF_8): String {
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
public suspend fun InputFlow.copyToOutputFlow(output: OutputFlow): Long = copyTo(output)

@ExperimentalUnsignedTypes
public suspend fun InputFlow.copyTo(output: OutputFlow, bufferSize: Int = 8192, dataSize: Int = Int.MAX_VALUE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    var bytesToCopy: Int
    while (bytes != null && bytesCopied < dataSize) {
        bytesToCopy = minOf(bytes, (dataSize - bytesCopied).toInt())
        output.write(buffer, 0, bytesToCopy)
        bytesCopied += bytesToCopy
        bytes = read(buffer)
    }
    return bytesCopied
}

@ExperimentalUnsignedTypes
public suspend fun OutputFlow.copyFrom(input: InputFlow): Long = input.copyTo(this)
