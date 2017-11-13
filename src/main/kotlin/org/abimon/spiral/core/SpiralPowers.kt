package org.abimon.spiral.core

import com.github.kittinunf.fuel.core.Request
import net.npe.tga.TGAWriter
import org.abimon.spiral.core.objects.images.GXTByteColourOrder
import org.abimon.spiral.util.traceWithCaller
import org.abimon.visi.io.readPartialBytes
import org.abimon.visi.lang.exportStackTrace
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

typealias TripleHashMap<T, U, V> = HashMap<T, Pair<U, V>>

fun byteArrayOfInts(vararg ints: Int): ByteArray = ints.map { it.toByte() }.toByteArray()

fun <T, U, V> TripleHashMap<T, U, V>.put(t: T, u: U, v: V) = put(t, Pair(u, v))

//val bitsAndBobs: Map<Int, BooleanArray> = run {
//    var value: Map<Int, BooleanArray>? = null
//    val time = measureNanoTime {
//        val bitRange = (0 until 8).map { 1 shl it }
//        value = (0 until 256).map { byte -> byte to bitRange.map { bit -> byte and bit == bit }.toBooleanArray() }.toMap()
//    }
//
//    println("Settings bits took $time")
//    return@run value ?: HashMap()
//}

fun Int.getBit(bit: Int): Byte {
    val shift = (1 shl bit)
    if (this and shift == shift)
        return 1
    return 0
}

fun ByteArray.toIntArray(): IntArray = map { it.toInt() and 0xFF }.toIntArray()

fun InputStream.readUnsignedLittleInt(): Long = readNumber(4, true, true)
fun InputStream.readUnsignedBigInt(): Long = readNumber(4, true, false)
fun InputStream.readUnsignedLittleFloat(): Float = readFloat(true, true)

val BITS_LOOKUP_TABLE: IntArray by lazy { (0 until 256).map { it * 8 }.toIntArray() }

fun InputStream.readLong(unsigned: Boolean = false, little: Boolean = true): Long = readNumber(8, unsigned, little)
fun InputStream.readInt(unsigned: Boolean = false, little: Boolean = true): Long = readNumber(4, unsigned, little)
fun InputStream.readShort(unsigned: Boolean = false, little: Boolean = true): Int = readNumber(2, unsigned, little).toInt()

fun InputStream.readNumber(bytes: Int = 4, unsigned: Boolean = false, little: Boolean = true): Long {
    var r = 0L
    val nums = ByteArray(bytes.coerceAtMost(BITS_LOOKUP_TABLE.size))
    val numRead = read(nums)

    if (little)
        nums.reverse()

    for (i in 0 until bytes)
        r = r or ((if (unsigned) nums[i].toInt() and 0xFF else nums[i].toInt()).toLong() shl BITS_LOOKUP_TABLE[bytes - 1 - i])

    return r
}

fun InputStream.readUnsureLong(unsigned: Boolean = false, little: Boolean = true): Long? = readUnsureNumber(8, unsigned, little)
fun InputStream.readUnsureInt(unsigned: Boolean = false, little: Boolean = true): Long? = readUnsureNumber(4, unsigned, little)
fun InputStream.readUnsureShort(unsigned: Boolean = false, little: Boolean = true): Int? = readUnsureNumber(2, unsigned, little)?.toInt()

fun InputStream.readUnsureNumber(bytes: Int = 4, unsigned: Boolean = false, little: Boolean = true): Long? {
    var r = 0L
    val nums = ByteArray(bytes.coerceAtMost(BITS_LOOKUP_TABLE.size))
    val numRead = read(nums)

    if (numRead == -1)
        return null

    if (little)
        nums.reverse()

    for (i in 0 until bytes)
        r = r or ((if (unsigned) nums[i].toInt() and 0xFF else nums[i].toInt()).toLong() shl BITS_LOOKUP_TABLE[bytes - 1 - i])

    return r
}

fun InputStream.readFloat(unsigned: Boolean = false, little: Boolean = true): Float = java.lang.Float.intBitsToFloat(this.readNumber(4, unsigned, little).toInt())

fun InputStream.readString(len: Int, encoding: String = "UTF-8"): String {
    val data = ByteArray(len.coerceAtLeast(0))
    read(data)
    return String(data, Charset.forName(encoding))
}

fun InputStream.readDRString(len: Int, encoding: String = "UTF-8"): String {
    val data = ByteArray(len.coerceAtLeast(0))
    read(data)
    return String(data.sliceArray(0 until data.size - 2), Charset.forName(encoding))
}

fun InputStream.readZeroString(maxLen: Int = 255, encoding: String = "UTF-8", bytesPerCharacter: Int = 1): String {
    val max = maxLen.coerceAtMost(255)
    val baos = ByteArrayOutputStream()

    for (i in 0 until max) {
        val read = readPartialBytes(bytesPerCharacter)
        if (read.all { it.toInt() == 0 })
            break

        baos.write(read)
    }

    return String(baos.toByteArray(), Charset.forName(encoding))
}

fun BufferedImage.toByteArray(format: String = "PNG"): ByteArray {
    val baos = ByteArrayOutputStream()

    ImageIO.write(this, format, baos)

    return baos.toByteArray()
}

fun BufferedImage.toTGA(): ByteArray = TGAWriter.writeImage(this)

fun BufferedImage.toJPG(): BufferedImage {
    val copy = BufferedImage(width, height, TYPE_INT_RGB)
    copy.graphics.drawImage(this, 0, 0, null)
    return copy
}

fun BufferedImage.toPNG(): BufferedImage {
    val copy = BufferedImage(width, height, TYPE_INT_ARGB)
    copy.graphics.drawImage(this, 0, 0, null)
    return copy
}

operator fun AtomicInteger.inc(): AtomicInteger {
    this.set(this.get() + 1)
    return this
}

fun OutputStream.writeNumber(num: Number, bytesNum: Int, unsigned: Boolean = false, little: Boolean = true) {
    when (bytesNum) {
        8 -> writeLong(num, unsigned, little)
        4 -> writeInt(num, unsigned, little)
        2 -> writeShort(num, unsigned, little)
        1 -> write(num.toByte().toInt())
        else -> {
            val long = num.toLong()
            val bytes = bytesNum.coerceAtMost(BITS_LOOKUP_TABLE.size)
            if (unsigned && little) {
                for (i in 0 until bytes)
                    write((long ushr BITS_LOOKUP_TABLE[i]).toByte().toInt())
            } else if (!unsigned && little) {
                for (i in 0 until bytes)
                    write((long shr BITS_LOOKUP_TABLE[i]).toByte().toInt())
            } else if (unsigned && !little) {
                for (i in (0 until bytes).reversed())
                    write((long ushr BITS_LOOKUP_TABLE[i]).toByte().toInt())
            } else {
                for (i in (0 until bytes).reversed())
                    write((long shr BITS_LOOKUP_TABLE[i]).toByte().toInt())
            }
        }
    }
}

fun OutputStream.writeLong(num: Number, unsigned: Boolean = false, little: Boolean = true) {
    val long = num.toLong()
    if (unsigned && little)
        write(byteArrayOf(long.toByte(), (long ushr 8).toByte(), (long ushr 16).toByte(), (long ushr 24).toByte(), (long ushr 32).toByte(), (long ushr 40).toByte(), (long ushr 48).toByte(), (long ushr 56).toByte()))
    else if (!unsigned && little)
        write(byteArrayOf(long.toByte(), (long shr 8).toByte(), (long shr 16).toByte(), (long shr 24).toByte(), (long shr 32).toByte(), (long shr 40).toByte(), (long shr 48).toByte(), (long shr 56).toByte()))
    else if (unsigned && !little)
        write(byteArrayOf((long ushr 56).toByte(), (long ushr 48).toByte(), (long ushr 40).toByte(), (long ushr 32).toByte(), (long ushr 24).toByte(), (long ushr 16).toByte(), (long ushr 8).toByte(), long.toByte()))
    else
        write(byteArrayOf((long shr 56).toByte(), (long shr 48).toByte(), (long shr 40).toByte(), (long shr 32).toByte(), (long shr 24).toByte(), (long shr 16).toByte(), (long shr 8).toByte(), long.toByte()))
}

fun OutputStream.writeInt(num: Number, unsigned: Boolean = false, little: Boolean = true) {
    val int = num.toInt()
    if (unsigned && little)
        write(byteArrayOfInts(int, int ushr 8, int ushr 16, int ushr 24))
    else if (!unsigned && little)
        write(byteArrayOfInts(int, int shr 8, int shr 16, int shr 24))
    else if (unsigned && !little)
        write(byteArrayOfInts(int ushr 24, int ushr 16, int ushr 8, int))
    else
        write(byteArrayOfInts(int shr 24, int shr 16, int shr 8, int))
}

fun OutputStream.writeShort(num: Number, unsigned: Boolean = false, little: Boolean = true) {
    val int = num.toShort().toInt()
    if (unsigned && little)
        write(byteArrayOfInts(int, int ushr 8))
    else if (!unsigned && little)
        write(byteArrayOfInts(int, int shr 8))
    else if (unsigned && !little)
        write(byteArrayOfInts(int ushr 8, int))
    else
        write(byteArrayOfInts(int shr 8, int))
}

fun Long.write(unsigned: Boolean = false, little: Boolean = true): ByteArray {
    val long = this
    if (unsigned && little)
        return byteArrayOf(long.toByte(), (long ushr 8).toByte(), (long ushr 16).toByte(), (long ushr 24).toByte(), (long ushr 32).toByte(), (long ushr 40).toByte(), (long ushr 48).toByte(), (long ushr 56).toByte())
    else if (!unsigned && little)
        return byteArrayOf(long.toByte(), (long shr 8).toByte(), (long shr 16).toByte(), (long shr 24).toByte(), (long shr 32).toByte(), (long shr 40).toByte(), (long shr 48).toByte(), (long shr 56).toByte())
    else if (unsigned && !little)
        return byteArrayOf((long ushr 56).toByte(), (long ushr 48).toByte(), (long ushr 40).toByte(), (long ushr 32).toByte(), (long ushr 24).toByte(), (long ushr 16).toByte(), (long ushr 8).toByte(), long.toByte())
    else
        return byteArrayOf((long shr 56).toByte(), (long shr 48).toByte(), (long shr 40).toByte(), (long shr 32).toByte(), (long shr 24).toByte(), (long shr 16).toByte(), (long shr 8).toByte(), long.toByte())
}

fun Int.write(unsigned: Boolean = false, little: Boolean = true): ByteArray {
    val int = this
    if (unsigned && little)
        return byteArrayOfInts(int, int ushr 8, int ushr 16, int ushr 24)
    else if (!unsigned && little)
        return byteArrayOfInts(int, int shr 8, int shr 16, int shr 24)
    else if (unsigned && !little)
        return byteArrayOfInts(int ushr 24, int ushr 16, int ushr 8, int)
    else
        return byteArrayOfInts(int shr 24, int shr 16, int shr 8, int)
}

fun Short.write(unsigned: Boolean = false, little: Boolean = true): ByteArray {
    val int = this.toInt()
    if (unsigned && little)
        return byteArrayOfInts(int, int ushr 8)
    else if (!unsigned && little)
        return byteArrayOfInts(int, int shr 8)
    else if (unsigned && !little)
        return byteArrayOfInts(int ushr 8, int)
    else
        return byteArrayOfInts(int shr 8, int)
}
//
//fun Byte.write(): ByteArray = ByteArray(1, { this })

fun ByteArray.write(outputStream: OutputStream) {
    outputStream.write(this)
}

fun String.toDRBytes(): ByteArray {
    val bytes = toByteArray(Charsets.UTF_16LE)
    val drBytes = ByteArray(bytes.size + 4)
    drBytes[0] = 0xFF.toByte()
    drBytes[1] = 0xFE.toByte()
    drBytes[bytes.size + 2] = 0
    drBytes[bytes.size + 3] = 0
    System.arraycopy(bytes, 0, drBytes, 2, bytes.size)
    return drBytes
}

fun OutputStream.print(str: String, encoding: String = "UTF-8") = write(str.toByteArray(Charset.forName(encoding)))
fun OutputStream.println(str: String, encoding: String = "UTF-8") = write("$str\n".toByteArray(Charset.forName(encoding)))

infix fun ByteArray.equals(other: ByteArray): Boolean = Arrays.equals(this, other)
infix fun ByteArray.doesntEqual(other: ByteArray): Boolean = !(this equals other)

infix fun <T : Number> T.hasBitSet(bit: T): Boolean = (this.toLong() and bit.toLong()) == bit.toLong()

fun Request.userAgent(agent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:54.0) Gecko/20100101 Firefox/54.0"): Request = this.header("User-Agent" to agent)

fun tryUnsafe(action: () -> Boolean): Boolean {
    try {
        return action()
    } catch (th: Throwable) {
        traceWithCaller("Error: ${th.exportStackTrace().replace("\n", " / ")}")
    }

    return false
}

fun IntArray.swizzle(colourOrder: GXTByteColourOrder): Int {
    val (first, second, third, fourth) = this
    return when (colourOrder) {
        GXTByteColourOrder.ABGR -> toRGBA(a = first, b = second, g = third, r = fourth)
        GXTByteColourOrder.ARGB -> toRGBA(a = first, r = second, g = third, b = fourth)
        GXTByteColourOrder.RGBA -> toRGBA(r = first, g = second, b = third, a = fourth)
        GXTByteColourOrder.BGRA -> toRGBA(b = first, g = second, r = third, a = fourth)
        GXTByteColourOrder.BGR -> toRGBA(b = first, g = second, r = third, a = 255)
        GXTByteColourOrder.RGB -> toRGBA(r = first, g = second, b = third, a = 255)
        GXTByteColourOrder._1BGR -> toRGBA(b = third, g = second, r = first, a = 255)
        GXTByteColourOrder._1RGB -> toRGBA(r = third, g = second, b = first, a = 255)
        else -> toRGBA(first, second, third, fourth)
    }
    //return toRGBA(b = first, g = second, r = third, a = fourth)
}

fun toRGBA(r: Int, g: Int, b: Int, a: Int): Int = Color(r, g, b, a).rgb//(a and 0xFF) shl 24 or (r and 0xFF shl 16) or (g and 0xFF shl 8) or (b and 0xFF shl 0)