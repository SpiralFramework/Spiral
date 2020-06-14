package info.spiralframework.formats.common.compression

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.korneaNotEnoughData
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.common.flow.readExact
import org.abimon.kornea.io.common.readInt32BE

const val DRV3_COMP_MAGIC_NUMBER = 0x24434D50 //0x504d4324

const val INVALID_DRV3_MAGIC_NUMBER = 0xE003
const val INVALID_DRV3_SHIFT_MODE = 0xE004

const val DRV3_NOT_ENOUGH_DATA = "formats.compression.drv3.not_enough_data"
const val INVALID_DRV3_MAGIC_NUMBER_KEY = "formats.compression.drv3.invalid_magic"
const val INVALID_DRV3_SHIFT_MODE_KEY = "formats.compression.drv3.invalid_shift"

const val CLN = 0x4e4c4324
const val CL1 = 0x314c4324
const val CL2 = 0x324c4324
const val CR0 = 0x30524324

@ExperimentalUnsignedTypes
suspend fun decompressV3(context: SpiralContext, data: ByteArray): KorneaResult<ByteArray> {
    val flow = BinaryInputFlow(data)

    val magic = flow.readInt32BE() ?: return context.localisedNotEnoughData(DRV3_NOT_ENOUGH_DATA)
    if (magic != DRV3_COMP_MAGIC_NUMBER) return KorneaResult.errorAsIllegalArgument(INVALID_DRV3_MAGIC_NUMBER, context.localise(INVALID_DRV3_MAGIC_NUMBER_KEY, magic.toHexString()))

    val compressedSize = flow.readInt32BE() ?: return context.localisedNotEnoughData(DRV3_NOT_ENOUGH_DATA)
    flow.skip(8u)

    val decompressedSize = flow.readInt32BE() ?: return context.localisedNotEnoughData(DRV3_NOT_ENOUGH_DATA)
    val compressedSizeTwo = flow.readInt32BE() ?: return context.localisedNotEnoughData(DRV3_NOT_ENOUGH_DATA)
    flow.skip(4u)

    val unk = flow.readInt32BE() ?: return context.localisedNotEnoughData(DRV3_NOT_ENOUGH_DATA)
    val output = BinaryOutputFlow()

    while (flow.available() > 0u) {
        val mode = flow.readInt32BE() ?: return context.localisedNotEnoughData(DRV3_NOT_ENOUGH_DATA)

        if (mode != CLN && mode != CL1 && mode != CL2 && mode != CR0) return KorneaResult.errorAsIllegalArgument(INVALID_DRV3_SHIFT_MODE, context.localise(INVALID_DRV3_SHIFT_MODE_KEY, mode.toHexString()))

        val chunkDecompressedSize = flow.readInt32BE() ?: return korneaNotEnoughData()
        val chunkCompressedSize = flow.readInt32BE() ?: return korneaNotEnoughData()

        flow.skip(4u)

        val chunk = ByteArray(chunkCompressedSize - 0x10)
        requireNotNull(flow.readExact(chunk))

        if (mode == CR0) {
            output.write(chunk)
        } else {
            output.write(deflateChunkV3(context, chunk, mode))
        }
    }

    return KorneaResult.success(output.getData())
}

@ExperimentalUnsignedTypes
private fun deflateChunkV3(context: SpiralContext, chunk: ByteArray, mode: Int): ByteArray {
    with(context) {
        val output: MutableList<Byte> = ArrayList()

//        var flag = 1
        var p = 0

        val shift: Int = when(mode) {
            CLN -> 8
            CL1 -> 7
            CL2 -> 6
            else -> throw IllegalStateException()
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