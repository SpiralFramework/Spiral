package org.abimon.spiral.core.objects.compression

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

interface ICompression {
    val supportsChunking: Boolean

    fun isCompressed(dataSource: () -> InputStream): Boolean

    fun decompress(dataSource: () -> InputStream): ByteArray = ByteArrayOutputStream().apply { decompressToPipe(dataSource, this) }.toByteArray()
    fun decompressToPipe(dataSource: () -> InputStream, sink: OutputStream)

    fun prepareChunkStream(stream: InputStream) {}
    fun decompressStreamChunk(stream: InputStream): ByteArray = ByteArray(0)
}