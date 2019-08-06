package info.spiralframework.formats.compression

import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readUInt32LE
import info.spiralframework.base.util.readXBytes
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.use
import java.io.InputStream
import java.io.OutputStream

object DRVitaCompression : ICompression {
    val MAGIC_NUMBER = 0xA755AAFC.toInt()
    val GX3_MAGIC_NUMBER = 0x335847.toInt()

    override val supportsChunking: Boolean = false

    override fun isCompressed(dataSource: () -> InputStream): Boolean =
            dataSource.use { stream ->
                val magic = stream.readInt32LE()

                if (magic == MAGIC_NUMBER)
                    return@use true
                if (magic == GX3_MAGIC_NUMBER)
                    return@use stream.readInt32LE() == MAGIC_NUMBER

                return@use false
            }

    override fun decompressToPipe(dataSource: () -> InputStream, sink: OutputStream) {
        sink.write(decompress(dataSource))
    }

    override fun decompress(dataSource: () -> InputStream): ByteArray =
            dataSource.use { stream ->
                val magic = stream.readInt32LE()
                if (magic == GX3_MAGIC_NUMBER)
                    stream.skip(4)

                val rawSize = stream.readUInt32LE()
                val compressedSize = stream.readUInt32LE().toLong()

                var i = 12
                var previousOffset = 1
                val result = ArrayList<Byte>()

                while (i < compressedSize) {
                    var b = stream.read()
                    i++

                    val bit1 = (b and 0b10000000) == 0b10000000 //128 / 2^7
                    val bit2 = (b and 0b01000000) == 0b01000000 //64 / 2^6
                    val bit3 = (b and 0b00100000) == 0b00100000 //32 / 2^5

                    if (bit1) {
                        val b2 = stream.read()
                        i++

                        val count = ((b ushr 5) and 0b011) + 4
                        val offset = ((b and 0b00011111) shl 8) + b2
                        previousOffset = offset

                        (0 until count).forEach { result.add(result[result.size - offset]) }
                    } else if (bit2 && bit3) {
                        val count = (b and 0b00011111)
                        val offset = previousOffset

                        (0 until count).forEach { result.add(result[result.size - offset]) }
                    } else if (bit2 && !bit3) {
                        var count = (b and 0b00001111)
                        if ((b and 0b00010000) == 0b00010000) {
                            b = stream.read()
                            i++
                            count = (count shl 8) + b
                        }

                        count += 4
                        b = stream.read()
                        i++

                        (0 until count).forEach { result.add(b.toByte()) }
                    } else if (!bit1 && !bit2) {
                        var count = (b and 0b00011111)
                        if (bit3) {
                            b = stream.read()
                            i++
                            count = (count shl 8) + b
                        }

                        if (count > 0)
                            result.addAll(stream.readXBytes(count).toList())

                        i += count
                    } else
                        DataHandler.LOGGER.error("formats.dr_vita.compression.invalid_bit_combination", bit1, bit2, bit3)
                }

                return@use result.toByteArray()
            }
}