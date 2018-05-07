package org.abimon.spiral.core.objects.compression

import org.abimon.spiral.core.utils.CriBitPool
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readInt64LE
import org.abimon.spiral.core.utils.readXBytes
import java.io.InputStream
import java.io.OutputStream

object CRILAYLACompression: ICompression {
    private val LOOKUP_TABLE: Map<Int, BooleanArray> = (0 until 256).map { int ->
        val bits = BooleanArray(8, { false })

        for(i in 0 until 8) {
            val pow = Math.pow(2.0, i.toDouble()).toInt()
            bits[7 - i] = (int and pow) == pow
        }

        return@map int to bits
    }.toMap()

    val MAGIC_NUMBER = 0x414c59414c495243 //"CRILAYLA"
    override val supportsChunking: Boolean = false

    override fun isCompressed(dataSource: () -> InputStream): Boolean = dataSource().use { stream -> stream.readInt64LE() == MAGIC_NUMBER }
    override fun decompressToPipe(dataSource: () -> InputStream, sink: OutputStream) {
        sink.write(decompress(dataSource))
    }

    override fun decompress(dataSource: () -> InputStream): ByteArray =
            dataSource().use { stream ->
                stream.skip(8)

                val uncompressedSize = stream.readInt32LE()
                val dataSize = stream.readInt32LE()

                val compressedData = stream.readXBytes(dataSize)
                val rawDataHeader = stream.readXBytes(0x100)

                return@use decompress(uncompressedSize, dataSize, compressedData, rawDataHeader)
            }

    /** This code is mostly unmodified, so just pray that it works */
    fun decompress(uncompressedSize: Int, dataSize: Int, compressedData: ByteArray, dataHeader: ByteArray): ByteArray {
        val outputEnd = 0x100 + uncompressedSize - 1

        var bytesOutput = 0
        val buffer = ByteArray(uncompressedSize + 0x100)

        val bitpool = CriBitPool(compressedData)

        dataHeader.forEachIndexed { index, byte -> buffer[index] = byte }

        while (bytesOutput < uncompressedSize) {
            if (bitpool[1] == 1) {
                var backreferenceOffset = outputEnd - bytesOutput + bitpool[13] + 3
                var backreferenceLength = 3

                val vle_lens = intArrayOf(2, 3, 5, 8)
                var vle_level = 0
                for (len in vle_lens) {
                    val this_level = bitpool[len]
                    backreferenceLength += this_level

                    if (this_level != ((1 shl len) - 1))
                        break

                    vle_level++
                }

                if (vle_level == vle_lens.size) {
                    var this_level: Int
                    do {
                        this_level = bitpool[8]
                        backreferenceLength += this_level
                    } while (this_level == 255)
                }

                for (i in 0 until backreferenceLength) {
                    buffer[(outputEnd - bytesOutput)] = buffer[backreferenceOffset]
                    backreferenceOffset--
                    bytesOutput++
                }
            } else {
                buffer[(outputEnd - bytesOutput)] = bitpool[8].toByte()
                bytesOutput++
            }
        }

        return buffer
    }
}