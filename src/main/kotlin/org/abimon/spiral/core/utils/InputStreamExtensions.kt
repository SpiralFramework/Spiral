package org.abimon.spiral.core.utils

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

fun InputStream.readString(len: Int, encoding: String = "UTF-8", overrideMaxLen: Boolean = false): String {
    val data = ByteArray(len.coerceAtLeast(0).run { if(!overrideMaxLen) this.coerceAtMost(1024 * 1024) else this })
    read(data)
    return String(data, Charset.forName(encoding))
}

fun InputStream.readXBytes(x: Int): ByteArray = ByteArray(x).apply { this@readXBytes.read(this) }

fun InputStream.readNullTerminatedString(maxLen: Int = 255, encoding: Charset = Charsets.UTF_8): String {
    val baos = ByteArrayOutputStream()
    var byte: Int

    while (true) {
        val read = read()
        if(read == 0x00 || read == -1)
            break

        baos.write(read)
    }

    return String(baos.toByteArray(), encoding)
}

fun InputStream.copyToStream(out: OutputStream): Unit { this.copyTo(out) }
fun OutputStream.copyFromStream(stream: InputStream): Unit { stream.copyTo(this) }

fun InputStream.copyWithProgress(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, progress: ((Long) -> Unit)?): Long {
    var bytesCopied = 0L
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        progress?.invoke(bytesCopied)
        bytes = read(buffer)
    }
    return bytesCopied
}

fun InputStream.readOrNull(): Int? {
    val read = read()
    if(read == -1)
        return null
    return read
}