package info.spiralframework.formats.common.compression

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.korneaNotEnoughData
import dev.brella.kornea.io.common.ReversedBitPoolInput
import dev.brella.kornea.io.common.flow.extensions.readInt64BE
import dev.brella.kornea.io.common.flow.extensions.readUInt32LE

public const val CRILAYLA_MAGIC: Long = 0x4352494C41594C41

public const val CRILAYLA_INVALID_MAGIC_NUMBER: Int = 0xE000

public fun decompressCrilayla(data: ByteArray): KorneaResult<ByteArray> {
    var pos = 0
    val magic = data.readInt64BE(pos) ?: return korneaNotEnoughData()
    if (magic != CRILAYLA_MAGIC) {
        return KorneaResult.errorAsIllegalArgument(CRILAYLA_INVALID_MAGIC_NUMBER, "Magic number 0x${magic.toString(16)} is invalid")
    }
    pos += 8

    val rawSize = data.readUInt32LE(pos)?.toInt() ?: return korneaNotEnoughData()
    pos += 4
    val compressedSize = data.readUInt32LE(pos)?.toInt() ?: return korneaNotEnoughData()
    pos += 4

    val output: MutableList<Byte> = ArrayList(data.slice(pos + compressedSize until pos + compressedSize + 0x100))
    output.addAll(deflateCrilayla(data.sliceArray(pos until pos + compressedSize), rawSize, compressedSize))

    return KorneaResult.success(output.toByteArray())
}

public const val CRILAYLA_MINIMAL_REFLEN: Int = 3
public val CRILAYLA_DEFLATE_LEVELS: IntArray = intArrayOf(2, 3, 5, 8)

public fun deflateCrilayla(compressedData: ByteArray, rawSize: Int, compressedSize: Int): List<Byte> {
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