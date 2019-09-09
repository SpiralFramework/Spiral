package info.spiralframework.base.util

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

@Deprecated("Use InputFlow instead")
fun InputStream.readString(len: Int, encoding: String = "UTF-8", overrideMaxLen: Boolean = false): String {
    val data = ByteArray(len.coerceAtLeast(0).run { if (!overrideMaxLen) this.coerceAtMost(1024 * 1024) else this })
    read(data)
    return String(data, Charset.forName(encoding))
}

@Deprecated("Use InputFlow instead")
fun InputStream.readXBytes(x: Int): ByteArray = ByteArray(x).apply { this@readXBytes.read(this) }

@Deprecated("Use InputFlow instead")
fun InputStream.readNullTerminatedUTF8String(): String = readNullTerminatedString(encoding = Charsets.UTF_8)
@Deprecated("Use InputFlow instead")
fun InputStream.readNullTerminatedString(maxLen: Int = 255, encoding: Charset = Charsets.UTF_8, bytesPer: Int = 1): String {
    val baos = ByteArrayOutputStream()

    while (true) {
        val read = readIntXLE(bytesPer)
        if (read == -1)
            break
        if (read == 0x00)
            break

        baos.writeIntXLE(read, bytesPer)
    }

    return String(baos.toByteArray(), encoding)
}

@Deprecated("Use InputFlow instead")
fun InputStream.copyToStream(out: OutputStream): Unit {
    this.copyTo(out)
}

@Deprecated("Use InputFlow instead")
fun OutputStream.copyFromStream(stream: InputStream): Unit {
    stream.copyTo(this)
}

@Deprecated("Use InputFlow instead")
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

@Deprecated("Use InputFlow instead")
fun InputStream.readOrNull(): Int? {
    val read = read()
    if (read == -1)
        return null
    return read
}

@Deprecated("Use InputFlow instead")
fun InputStream.readChunked(bufferSize: Int = 8192, closeAfter: Boolean = true, processChunk: (ByteArray) -> Unit): Int {
    val buffer = ByteArray(bufferSize)
    var read = 0
    var total = 0
    var count = 0.toByte()

    while (read > -1) {
        read = read(buffer)
        if (read < 0)
            break
        if (read == 0) {
            count++
            if (count > 3)
                break
        }

        processChunk(buffer.copyOfRange(0, read))
        total += read
    }

    if (closeAfter)
        close()

    return total
}

@Deprecated("Use InputFlow instead")
fun (() -> InputStream).from(offset: Long): () -> InputStream = {
    val stream = this()
    stream.skip(offset)
    stream
}