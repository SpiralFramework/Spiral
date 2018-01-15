package org.abimon.spiral.core.utils

import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

fun InputStream.readString(len: Int, encoding: String = "UTF-8"): String {
    val data = ByteArray(len.coerceAtLeast(0))
    read(data)
    return String(data, Charset.forName(encoding))
}

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