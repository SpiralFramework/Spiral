package org.abimon.spiral.core.objects.compression

import org.abimon.spiral.core.utils.readInt32BE
import org.abimon.spiral.core.utils.readInt32LE
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object V3Compression: ICompression {
    val MAGIC_NUMBER = 0x504d4324

    val CLN = 0x4e4c4324
    val CL1 = 0x314c4324
    val CL2 = 0x324c4324
    val CR0 = 0x30524324

    override val supportsChunking = true

    override fun isCompressed(dataSource: () -> InputStream): Boolean = dataSource().use { stream -> stream.readInt32LE() == MAGIC_NUMBER }
    override fun decompressToPipe(dataSource: () -> InputStream, sink: OutputStream) {
        dataSource().use { stream ->
            stream.skip(4)

            val compressedSize = stream.readInt32BE()
            stream.skip(8)
            val decompressedSize = stream.readInt32BE()
            val compressedSize2 = stream.readInt32BE()
            stream.skip(4)
            val unk = stream.readInt32BE()

            while (true) {
                val chunk = decompressStreamChunk(stream)
                if (chunk.isEmpty())
                    break

                sink.write(chunk)
            }
        }
    }

    override fun prepareChunkStream(stream: InputStream) {
        stream.skip(32)
    }

    override fun decompressStreamChunk(stream: InputStream): ByteArray {
        val mode = stream.readInt32LE()

        if (mode != CLN && mode != CL1 && mode != CL2 && mode != CR0)
            return ByteArray(0)

        val chunkDecompressedSize = stream.readInt32BE()
        val chunkCompressedSize = stream.readInt32BE()
        stream.skip(4)

        val chunk = ByteArray(chunkCompressedSize - 0x10).apply { stream.read(this) }

        if (mode == CR0)
            return chunk
        else
            return decompressChunk(chunk, mode)
    }

    fun decompressChunk(data: ByteArray, mode: Int): ByteArray {
        val result: MutableList<Byte> = LinkedList()

        var flag = 1
        var p = 0

        val shift: Int

        when (mode) {
            CLN -> shift = 8
            CL1 -> shift = 7
            CL2 -> shift = 6
            else -> error("Unknown mode $mode")
        }

        val mask = (1 shl shift) - 1

        while (p < data.size) {
            val b = data[p].toInt() and 0xFF
            p++

            if (b and 1 == 1) {
                val count = (b and mask) shr 1
                val offset = ((b shr shift) shl 8) or (data[p].toInt() and 0xFF)
                p++

                for (i in 0 until count)
                    result.add(result[result.size - offset])
            } else {
                val count = b shr 1
                result.addAll(data.copyOfRange(p, p + count).toTypedArray())
                p += count
            }
        }

        return result.toByteArray()
    }
}