package org.abimon.spiral.core

import org.abimon.external.TGAWriter
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

typealias TripleHashMap<T, U, V> = HashMap<T, Pair<U, V>>

fun <T, U, V> TripleHashMap<T, U, V>.put(t: T, u: U, v: V) = put(t, Pair(u, v))

fun InputStream.readNumber(bytes: Int = 4, unsigned: Boolean = false): Long {
    var s = "0"

    try {
        val bin = Array(bytes, {""})
        for (i in 0 until bytes)
            bin[i] = (if (unsigned) read().toLong() and 0xffffffffL else read().toLong()).toBinaryString()

        val base = "00000000"
        for (i in if(unsigned) (bytes - 1 downTo 0) else (0 until bytes))
            s += base.substring(bin[i].length) + bin[i]
    } catch (th: Throwable) {
    }

    return s.toLong(2)
}

fun InputStream.readFloat(unsigned: Boolean = false): Float {
    var s = "0"

    try {
        val bin = Array(4, {""})
        for (i in 0 until 4)
            bin[i] = (if (unsigned) read().toLong() and 0xffffffffL else read().toLong()).toBinaryString()

        val base = "00000000"
        for (i in if(unsigned) (4 - 1 downTo 0) else (0 until 4))
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

fun InputStream.readZeroString(maxLen: Int = 255, encoding: String = "UTF-8"): String {
    val max = maxLen.coerceAtMost(255)
    val baos = ByteArrayOutputStream()

    for(i in 0 until max) {
        val read = read()
        if(read <= 0)
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

    for (i in if(unsigned) (bytes - 1 downTo 0) else (0 until bytes)) {
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

fun Byte.write(): ByteArray = ByteArray(1, {this})

fun ByteArray.write(outputStream: OutputStream) {
    outputStream.write(this)
}

fun String.toDRBytes(): ByteArray {
    val bytes = toByteArray(Charsets.UTF_16LE)
    val drBytes = ByteArray(bytes.size + 4)
    drBytes[0] = 0xFF.toByte()
    drBytes[1] = 0xFE.toByte()
    drBytes[bytes.size] = 0
    drBytes[bytes.size + 1] = 0
    System.arraycopy(bytes, 0, drBytes, 2, bytes.size)
    return drBytes
}

fun OutputStream.writeString(str: String, encoding: String = "UTF-8") = write(str.toByteArray(Charset.forName(encoding)))

fun String.getParents(): String {
    return if(this.lastIndexOf('/') == -1) "" else this.substring(0, this.lastIndexOf('/'))
}

fun String.getChild(): String {
    return if(this.lastIndexOf('/') == -1) this else this.substring(this.lastIndexOf('/') + 1, length)
}

fun String.getExtension(): String {
    return if(this.lastIndexOf('.') == -1) this else this.substring(this.lastIndexOf('.') + 1, length)
}

fun <T> T.asOptional(): Optional<T> = Optional.of(this)

inline fun <T> Array<out T>.findOrEmpty(predicate: (T) -> Boolean): Optional<T> {
    return Optional.ofNullable(firstOrNull(predicate))
}