package org.abimon.spiral.core

import com.github.kittinunf.fuel.core.Request
import net.npe.tga.TGAWriter
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.lang.toBinaryString
import java.awt.image.BufferedImage
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

fun debug(any: Any?) {
    if (SpiralModel.isDebug) println(any)
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

    return s.toLong(2).toFloat()
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

infix fun <T: Number> T.hasBitSet(bit: T): Boolean = (this.toLong() and bit.toLong()) == bit.toLong()

fun Request.userAgent(agent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:54.0) Gecko/20100101 Firefox/54.0"): Request = this.header("User-Agent" to agent)