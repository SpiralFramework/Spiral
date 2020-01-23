package info.spiralframework.formats.common.compression

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import info.spiralframework.base.common.io.flow.readExact
import info.spiralframework.base.common.io.readInt32BE

const val MAGIC_NUMBER = 0x24434D50 //0x504d4324

const val CLN = 0x4e4c4324
const val CL1 = 0x314c4324
const val CL2 = 0x324c4324
const val CR0 = 0x30524324

@ExperimentalUnsignedTypes
suspend fun decompressV3(context: SpiralContext, data: ByteArray): ByteArray {
    val flow = BinaryInputFlow(data)

    val magic = requireNotNull(flow.readInt32BE())
    require(magic == MAGIC_NUMBER)

    val compressedSize = requireNotNull(flow.readInt32BE())
    flow.skip(8u)

    val decompressedSize = requireNotNull(flow.readInt32BE())
    val compressedSizeTwo = requireNotNull(flow.readInt32BE())
    flow.skip(4u)

    val unk = requireNotNull(flow.readInt32BE())
    val output = BinaryOutputFlow()

    while (flow.available() > 0u) {
        val mode = requireNotNull(flow.readInt32BE())

        require(mode == CLN || mode == CL1 || mode == CL2 && mode == CR0)

        val chunkDecompressedSize = requireNotNull(flow.readInt32BE())
        val chunkCompressedSize = requireNotNull(flow.readInt32BE())

        flow.skip(4u)

        val chunk = ByteArray(chunkCompressedSize - 0x10)
        requireNotNull(flow.readExact(chunk))

        if (mode == CR0) {
            output.write(chunk)
        } else {
            output.write(deflateChunkV3(context, chunk, mode))
        }
    }

    return output.getData()
}

@ExperimentalUnsignedTypes
fun deflateChunkV3(context: SpiralContext, chunk: ByteArray, mode: Int): ByteArray {
    with(context) {
        val output: MutableList<Byte> = ArrayList()

//        var flag = 1
        var p = 0

        val shift: Int = when(mode) {
            CLN -> 8
            CL1 -> 7
            CL2 -> 6
            else -> throw IllegalArgumentException(localise("formats.v3_compression.invalid_mode", mode))
        }

        val mask = (1 shl shift) - 1

        while (p < chunk.size) {
            val b = chunk[p++].toInt() and 0xFF

            if (b and 1 == 1) {
                val count = (b and mask) shr 1
                val offset = ((b shr shift) shl 8) or (chunk[p++].toInt() and 0xFF)

                for (i in 0 until count)
                    output.add(output[output.size - offset])
            } else {
                val count = b shr 1
                output.addAll(chunk.slice(p until p + count))
                p += count
            }
        }

        return output.toByteArray()
    }
}