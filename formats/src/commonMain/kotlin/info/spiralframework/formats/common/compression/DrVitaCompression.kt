package info.spiralframework.formats.common.compression

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.korneaNotEnoughData
import dev.brella.kornea.io.common.flow.extensions.readUInt32LE

const val DR_VITA_MAGIC = 0xA755AAFCu
const val DR_VITA_GX3_MAGIC = 0x335847

const val DR_VITA_INVALID_MAGIC_NUMBER = 0xE001

@ExperimentalUnsignedTypes
fun decompressVita(data: ByteArray): KorneaResult<ByteArray> {
    var pos = 0
    val magic = data.readUInt32LE(pos) ?: return korneaNotEnoughData()
    if(magic != DR_VITA_MAGIC) {
        return KorneaResult.errorAsIllegalArgument(DR_VITA_INVALID_MAGIC_NUMBER, "Magic number 0x${magic.toString(16)} is invalid")
    }

    pos += 4

    val rawSize = data.readUInt32LE(pos) ?: return korneaNotEnoughData()
    pos += 4
    val compressedSize = data.readUInt32LE(pos)?.toLong() ?: return korneaNotEnoughData()
    pos += 4

    var previousOffset = 1
    val output = ArrayList<Byte>()

    while (pos < compressedSize) {
        val b = data[pos++].toInt() and 0xFF
        val bit1 = (b and 0b10000000) == 0b10000000 //128 / 0x80 / 2^7
        val bit2 = (b and 0b01000000) == 0b01000000 //64  / 0x40 / 2^6
        val bit3 = (b and 0b00100000) == 0b00100000 //32  / 0x20 / 2^5

        if (bit1) {
            val b2 = data[pos++].toInt() and 0xFF
            val count = ((b ushr 5) and 0b011) + 4
            val offset = ((b and 0b00011111) shl 8) or b2
            previousOffset = offset

            for (i in 0 until count) {
                try {
                    output.add(output[output.size - offset])
                } catch (ioob: IndexOutOfBoundsException) {
                    throw ioob
                }
            }
        } else if (bit2 && bit3) {
            val count = (b and 0b00011111)
            val offset = previousOffset

            for (i in 0 until count) {
                output.add(output[output.size - offset])
            }
        } else if (bit2) {
            require(!bit3)

            val count: Int =
                    if ((b and 0b00010000) == 0b00010000) {
                        ((b and 0b00001111) shl 8) or data[pos++].toInt().and(0xFF)
                    } else {
                        b and 0b00001111
                    }

            val adding = data[pos++]
            val array = Array(count + 4) { adding }
            output.addAll(array)
        } else {
            require(!bit1 && !bit2)

            val count: Int =
                    if (bit3) {
                        ((b and 0b00011111) shl 8) or data[pos++].toInt().and(0xFF)
                    } else {
                        b and 0b00011111
                    }

            if (count > 0) {
                output.addAll(data.slice(pos until pos + count))
                pos += count
            }
        }
    }

//    require(output.size == rawSize.toInt())

    return KorneaResult.success(output.toByteArray(), null)
}