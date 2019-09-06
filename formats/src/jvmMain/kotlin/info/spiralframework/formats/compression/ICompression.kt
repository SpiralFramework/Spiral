package info.spiralframework.formats.compression

import info.spiralframework.base.common.SpiralContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

interface ICompression {
    val supportsChunking: Boolean

    fun isCompressed(context: SpiralContext, dataSource: () -> InputStream): Boolean

    fun decompress(context: SpiralContext, dataSource: () -> InputStream): ByteArray = ByteArrayOutputStream().apply { decompressToPipe(context, dataSource, this) }.toByteArray()
    fun decompressToPipe(context: SpiralContext, dataSource: () -> InputStream, sink: OutputStream)

    fun prepareChunkStream(context: SpiralContext, stream: InputStream) {}
    fun decompressStreamChunk(context: SpiralContext, stream: InputStream): ByteArray = ByteArray(0)
}