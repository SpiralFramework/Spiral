package org.abimon.spiral.core

import net.npe.tga.TGAWriter
import org.abimon.visi.lang.toBinaryString
import org.abimon.visi.lang.toFloat
import org.abimon.visi.lang.toLong
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import kotlin.experimental.and
import kotlin.system.measureNanoTime

typealias TripleHashMap<T, U, V> = HashMap<T, Pair<U, V>>

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

fun debug(any: Any?) {
    if (isDebug) println(any)
}

fun InputStream.readUnsignedLittleInt(): Long = readNumber(4, true, true)

fun InputStream.readNumber(bytes: Int = 4, unsigned: Boolean = false, little: Boolean = true): Long {
    var s = "0"

    try {
        val bin = Array(bytes, { "" })
        for (i in 0 until bytes)
            bin[i] = (if (unsigned) read().toLong() and 0xffffffffL else read().toLong()).toBinaryString()

        val base = "00000000"
        for (i in if (little) (bytes - 1 downTo 0) else (0 until bytes))
            s += base.substring(bin[i].length) + bin[i]
    } catch (th: Throwable) {
    }

    return s.toLong(2)
}

fun InputStream.readFloat(unsigned: Boolean = false, little: Boolean = true): Float {
    var s = "0"

    try {
        val bin = Array(4, { "" })
        for (i in 0 until 4)
            bin[i] = (if (unsigned) read().toLong() and 0xffffffffL else read().toLong()).toBinaryString()

        val base = "00000000"
        for (i in if (little) (4 - 1 downTo 0) else (0 until 4))
            s += base.substring(bin[i].length) + bin[i]
    } catch (th: Throwable) {
    }

    return s.toFloat(2)
}

fun InputStream.readString(len: Int, encoding: String = "UTF-8"): String {
    val data: ByteArray = ByteArray(len.coerceAtLeast(0))
    read(data)
    return String(data, Charset.forName(encoding))
}

fun InputStream.readDRString(len: Int, encoding: String = "UTF-8"): String {
    val data: ByteArray = ByteArray(len.coerceAtLeast(0))
    read(data)
    return String(data.sliceArray(0 until data.size - 2), Charset.forName(encoding))
}

fun InputStream.readZeroString(maxLen: Int = 255, encoding: String = "UTF-8"): String {
    val max = maxLen.coerceAtMost(255)
    val baos = ByteArrayOutputStream()

    for (i in 0 until max) {
        val read = read()
        if (read <= 0)
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

operator fun AtomicInteger.inc(): AtomicInteger {
    this.set(this.get() + 1)
    return this
}

fun OutputStream.writeNumber(num: Long, bytes: Int = 4, unsigned: Boolean = false) {
    val ss = num.toBinaryString()
    var base = ""
    for (i in 0 until bytes)
        base += "00000000"
    val nums = base.substring(ss.length) + ss

    for (i in if (unsigned) (bytes - 1 downTo 0) else (0 until bytes)) {
        write(Integer.parseInt(nums.substring(i * 8, (i + 1) * 8), 2))
    }
}

fun Long.write(unsigned: Boolean = false): ByteArray {
    val baos = ByteArrayOutputStream()
    baos.writeNumber(this, 8, unsigned)
    return baos.toByteArray()
}

fun Int.write(unsigned: Boolean = false): ByteArray {
    val baos = ByteArrayOutputStream()
    baos.writeNumber(this.toLong(), 4, unsigned)
    return baos.toByteArray()
}

fun Short.write(unsigned: Boolean = false): ByteArray {
    val baos = ByteArrayOutputStream()
    baos.writeNumber(this.toLong(), 2, unsigned)
    return baos.toByteArray()
}

fun Byte.write(): ByteArray = ByteArray(1, { this })

fun ByteArray.write(outputStream: OutputStream) {
    outputStream.write(this)
}

fun String.toDRBytes(): ByteArray {
    val bytes = toByteArray(Charsets.UTF_16LE)
    val drBytes = ByteArray(bytes.size + 2)
    drBytes[bytes.size] = 0
    drBytes[bytes.size + 1] = 0
    System.arraycopy(bytes, 0, drBytes, 0, bytes.size)
    return drBytes
}

fun OutputStream.print(str: String, encoding: String = "UTF-8") = write(str.toByteArray(Charset.forName(encoding)))
fun OutputStream.println(str: String, encoding: String = "UTF-8") = write("$str\n".toByteArray(Charset.forName(encoding)))

fun String.getParents(): String {
    return if (this.lastIndexOf('/') == -1) "" else this.substring(0, this.lastIndexOf('/'))
}

fun String.getChild(): String {
    return if (this.lastIndexOf('/') == -1) this else this.substring(this.lastIndexOf('/') + 1, length)
}

fun String.getExtension(): String {
    return if (this.lastIndexOf('.') == -1) this else this.substring(this.lastIndexOf('.') + 1, length)
}

fun <T> T.asOptional(): Optional<T> = Optional.of(this)

inline fun <T> Array<out T>.findOrEmpty(predicate: (T) -> Boolean): Optional<T> {
    return Optional.ofNullable(firstOrNull(predicate))
}

public infix fun <A, B, C> Pair<A, B>.and(that: C): Triple<A, B, C> = Triple(this.first, this.second, that)

fun byteArrayOf(vararg bytes: Int): ByteArray = kotlin.byteArrayOf(*bytes.map { it.toByte() }.toByteArray())

fun InputStream.read(count: Int): ByteArray {
    val data = ByteArray(count)
    val read = read(data)
    return data.copyOfRange(0, read)
}

infix fun ByteArray.equals(other: ByteArray): Boolean = Arrays.equals(this, other)
infix fun ByteArray.doesntEqual(other: ByteArray): Boolean = !(this equals other)

infix fun ByteArray.asBase(base: Int): String = this.joinToString(" ") { byte ->
    when(base) {
        2 -> "0b${byte.toString(2)}"
        16 -> "0x${byte.toString(16)}"
        else -> byte.toString(base)
    }
}

infix fun <T: Number> T.hasBitSet(bit: T): Boolean = (this.toLong() and bit.toLong()) == bit.toLong()