package info.spiralframework.formats.common.compression

import info.spiralframework.base.common.io.ReversedBitPoolInput
import info.spiralframework.base.common.io.readInt64BE
import info.spiralframework.base.common.io.readUInt32LE

const val CRILAYLA_MAGIC = 0x4352494C41594C41

@ExperimentalUnsignedTypes
fun decompressCrilayla(data: ByteArray): ByteArray {
    var pos = 0
    val magic = requireNotNull(data.readInt64BE(pos))
    require(magic == CRILAYLA_MAGIC) { "Magic number 0x${magic.toString(16)} is invalid" }
    pos += 8

    val rawSize = requireNotNull(data.readUInt32LE(pos)).toInt()
    pos += 4
    val compressedSize = requireNotNull(data.readUInt32LE(pos)).toInt()
    pos += 4

    val output: MutableList<Byte> = ArrayList(data.slice(pos + compressedSize until pos + compressedSize + 0x100))
    output.addAll(deflateCrilayla(data.sliceArray(pos until pos + compressedSize), rawSize, compressedSize))

    return output.toByteArray()
}

const val CRILAYLA_MINIMAL_REFLEN = 3
val CRILAYLA_DEFLATE_LEVELS = intArrayOf(2, 3, 5, 8)

fun deflateCrilayla(compressedData: ByteArray, rawSize: Int, compressedSize: Int): List<Byte> {
    val bitpool = ReversedBitPoolInput(compressedData, compressedSize)
    val output: MutableList<Byte> = ArrayList(rawSize)

    while (!bitpool.isEmpty && output.size < rawSize) {
        if (bitpool.read(1) == 1) {
            val offset = bitpool.read(13) + CRILAYLA_MINIMAL_REFLEN
            var refc = CRILAYLA_MINIMAL_REFLEN

            var lvIndex = 0
            var lv: Int
            var bits: Int
            do {
                lv = CRILAYLA_DEFLATE_LEVELS.getOrNull(lvIndex++) ?: 8
                bits = bitpool.read(lv)
                refc += bits
            } while (bits == ((1 shl lv) - 1))

            for (i in 0 until refc) {
                output.add(output[output.size - offset])
            }

//            output.addAll(output.subList(output.size - offset, output.size - offset + refc))
        } else {
            //verbatim byte
            output.add(bitpool.read(8).toByte())
        }
    }

    return output.reversed()
}