package org.abimon.spiral.core.formats.compression

import org.abimon.spiral.core.*
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.visi.collections.asBase
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*

object DRVitaCompressionFormat : SpiralFormat {
    override val name: String = "Danganronpa Vita Compression"
    override val extension: String? = null
    override val conversions: Array<SpiralFormat> = arrayOf(SpiralFormat.BinaryFormat)

    val CMP_MAGIC = byteArrayOfInts(0xFC, 0xAA, 0x55, 0xA7)
    val GX3_MAGIC = byteArrayOfInts(0x47, 0x58, 0x33, 0x00)

    override fun isFormat(source: DataSource): Boolean = tryUnsafe { source.use { it.read(4) equals CMP_MAGIC } }

    override fun canConvert(format: SpiralFormat): Boolean = true

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        source.use { stream ->
            var magic = stream.read(4)

            if (magic equals GX3_MAGIC)
                magic = stream.read(4)

            if (magic doesntEqual CMP_MAGIC)
                throw IllegalArgumentException("${source.location} does not conform to the ${name} format (Magic Number ≠ ${CMP_MAGIC asBase 16}; is actually ${magic asBase 16})")

            val rawSize = stream.readUnsignedLittleInt()
            val compressedSize = stream.readUnsignedLittleInt()

            var i = 12
            var previousOffset = 1
            val result = ArrayList<Byte>()

            while (i < compressedSize) {
                var b = stream.read()
                i++

                val bit1 = b hasBitSet 0b10000000 //128 / 2^7
                val bit2 = b hasBitSet 0b01000000 //64 / 2^6
                val bit3 = b hasBitSet 0b00100000 //32 / 2^5

                if (bit1) {
                    val b2 = stream.read()
                    i++

                    val count = ((b ushr 5) and 0b011) + 4
                    val offset = ((b and 0b00011111) shl 8) + b2
                    previousOffset = offset

                    (0 until count).forEach { result.add(result[result.size-offset]) }
                } else if (bit2 && bit3) {
                    val count = (b and 0b00011111)
                    val offset = previousOffset

                    (0 until count).forEach { result.add(result[result.size-offset]) }
                } else if (bit2 && !bit3) {
                    var count = (b and 0b00001111)
                    if (b hasBitSet 0b00010000) {
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

                    if(count > 0)
                        result.addAll(stream.read(count).toList())

                    i += count
                } else
                    println("???")
            }

            if(result.size.toLong() != rawSize)
                println("Different sizes (Expected $rawSize, got ${result.size})")

            output.write(result.toByteArray())
        }

        return true
    }

    override fun convertFrom(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(format.canConvert(this)) //Check if there's a built in way of doing it
            return format.convert(this, source, output, params)
        else { //Otherwise we roll up our sleeves and get dirty
            source.use { stream ->
                val result = ByteArrayOutputStream()

                val buffer = ByteArray(15)
                var size = 0
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0)
                        break
                    size += read
                    result.write(read)
                    result.write(buffer, 0, read)

                    if (read != 15)
                        break
                }

                output.write(0xFC)
                output.write(0xAA)
                output.write(0x55)
                output.write(0xA7)

                output.writeInt(size.toLong(), true, true)
                output.writeInt(result.size() + 12L, true, true)

                result.writeTo(output)
            }

            return true
        }
    }
}