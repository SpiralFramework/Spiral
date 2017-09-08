package org.abimon.spiral.core.formats

import org.abimon.spiral.core.data.BitPool
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.visi.io.DataSource
import java.io.OutputStream

object CRILAYLAFormat : SpiralFormat {
    override val name: String = "CRILAYLA"
    override val extension: String? = null
    override val conversions: Array<SpiralFormat> = arrayOf(SpiralFormat.BinaryFormat)

    override fun isFormat(source: DataSource): Boolean = source.use { it.readString(8) == "CRILAYLA" }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)

        source.use { stream ->
            val magic = stream.readString(8)

            if (magic != "CRILAYLA") {
                throw IllegalArgumentException("${source.location} does not conform to the $name format")
            } else {
                val uncompressedSize = stream.readUnsignedLittleInt()
                val datasize = stream.readUnsignedLittleInt()

                val compressedData: ByteArray = ByteArray(datasize.toInt()).apply { stream.read(this) }
                val rawDataHeader: ByteArray = ByteArray(0x100).apply { stream.read(this) }

                val outputEnd = 0x100 + uncompressedSize - 1

                var bytesOutput = 0
                val buffer = ByteArray((uncompressedSize + 0x100).toInt())

                val bitpool = BitPool(compressedData)

                rawDataHeader.forEachIndexed { index, byte -> buffer[index] = byte }

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
                            buffer[(outputEnd - bytesOutput).toInt()] = buffer[backreferenceOffset.toInt()]
                            backreferenceOffset--
                            bytesOutput++
                        }

                    } else {
                        buffer[(outputEnd - bytesOutput).toInt()] = bitpool[8].toByte()
                        bytesOutput++
                    }
                }

                output.write(buffer)
            }
        }
    }

//    fun something() {
//        source.seekableInputStream.use {
//            val stream = SeekableInputStream(it)
//            val magic = stream.readString(8)
//
//            if (magic != "CRILAYLA") {
//                println("err :/")
//            } else {
//                val uncompressedSize = stream.readUnsignedLittleInt()
//                val datasize = stream.readUnsignedLittleInt()
//
//                val compressedData: ByteArray = ByteArray(datasize.toInt()).apply { stream.read(this) }
//                val rawDataHeader: ByteArray = ByteArray(0x100).apply { stream.read(this) }
//
//                val outputEnd = 0x100 + uncompressedSize - 1
//
//                var bytesOutput = 0
//                val buffer = ByteArray((uncompressedSize + 0x100).toInt())
//
//                val bitpool = BitPool(compressedData)
//
//                stream.seek(0x10 + datasize)
//                for (i in 0 until 0x100)
//                    buffer[i] = it.read().toByte()
//
//                while (bytesOutput < uncompressedSize) {
//                    if (bitpool[1] == 1) {
//                        var backreferenceOffset = outputEnd - bytesOutput + bitpool[13] + 3
//                        var backreferenceLength = 3
//
//                        val vle_lens = intArrayOf(2, 3, 5, 8)
//                        var vle_level = 0
//                        for (len in vle_lens) {
//                            val this_level = bitpool[len]
//                            backreferenceLength += this_level
//
//                            if (this_level != ((1 shl len) - 1))
//                                break
//
//                            vle_level++
//                        }
//
//                        if (vle_level == vle_lens.size) {
//                            var this_level: Int
//                            do {
//                                this_level = bitpool[8]
//                                backreferenceLength += this_level
//                            } while (this_level == 255)
//                        }
//
//                        for (i in 0 until backreferenceLength) {
//                            buffer[(outputEnd - bytesOutput).toInt()] = buffer[backreferenceOffset.toInt()]
//                            backreferenceOffset--
//                            bytesOutput++
//                        }
//
//                    } else {
//                        buffer[(outputEnd - bytesOutput).toInt()] = bitpool[8].toByte()
//                        bytesOutput++
//                    }
//                }
//
//                File("bustup_00_00.done.gxt").writeBytes(buffer)
//            }
//        }
//    }
}