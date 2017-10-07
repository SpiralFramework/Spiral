package org.abimon.spiral.core.formats

import org.abimon.spiral.core.data.BitPool
import org.abimon.spiral.core.hasBitSet
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.spiral.core.writeNumber
import org.abimon.visi.io.DataSource
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*

object CRILAYLAFormat : SpiralFormat {
    override val name: String = "CRILAYLA"
    override val extension: String? = null
    override val conversions: Array<SpiralFormat> = arrayOf(SpiralFormat.BinaryFormat)

    private val LOOKUP_TABLE: Map<Int, BooleanArray> = (0 until 256).map { int ->
        val bits = BooleanArray(8, { false })

        for(i in 0 until 8)
            bits[7 - i] = int hasBitSet Math.pow(2.0, i.toDouble())

        return@map int to bits
    }.toMap()

    val MAGIC = "CRILAYLA"
    val MAGIC_BYTES = MAGIC.toByteArray()

    override fun isFormat(source: DataSource): Boolean = source.use { it.readString(8) == MAGIC }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        source.use { stream ->
            val magic = stream.readString(8)

            if (magic != MAGIC) {
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

                println(bitpool)

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

        return true
    }

    override fun convertFrom(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(format.canConvert(this)) //Check if there's a build in way
            return format.convert(this, source, output, params)
        else { //Let's get our hands dirty
            val sourceData = source.data
            if(sourceData.size < 0x100) {
                output.write(sourceData)
                return false
            }
            val data: Queue<Boolean> = sourceData.copyOfRange(0x100, sourceData.size).reversed().flatMap { arrayListOf(false).apply { LOOKUP_TABLE[it.toInt() and 0xFF]!!.toCollection(this) } }.toCollection(LinkedList())
            output.write(MAGIC_BYTES)
            output.writeNumber(sourceData.size - 0x100L, 4, true)
            val baos = ByteArrayOutputStream()

            while(data.isNotEmpty()) {
                val seven = BooleanArray(8, { false })
                for(i in 0 until 8)
                    seven[i] = data.poll() ?: false

                baos.write(LOOKUP_TABLE.entries.first { (_, bits) -> bits contentEquals seven }.key)
            }
            output.writeNumber(baos.size().toLong(), 4, true)
            output.write(baos.toByteArray().reversedArray())
            output.write(sourceData.copyOfRange(0, 0x100))

            return true
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