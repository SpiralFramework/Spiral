package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.cast
import org.abimon.kornea.errors.common.getOrBreak
import org.abimon.kornea.errors.common.filterToInstance
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.*
import org.kornea.toolkit.common.oneTimeMutableInline

typealias VertexBlock = RSISrdEntry.ResourceIndex
typealias IndexBlock = RSISrdEntry.ResourceIndex
typealias FaceBlock = RSISrdEntry.ResourceIndex

@ExperimentalUnsignedTypes
/** Original Work Do Not Steal */
data class VTXSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24565458
    }

    data class VertexSizePair(val offset: Int, val size: Int)

    var rsiEntry: RSISrdEntry by oneTimeMutableInline()
    val vertexBlock: VertexBlock
        get() = rsiEntry.resources[0]
    val faceBlock: FaceBlock
        get() = rsiEntry.resources[1]

    var unk1: Int by oneTimeMutableInline()
    var unk2: Int by oneTimeMutableInline()
    var unk3: Int by oneTimeMutableInline()

    var vertexCount: Int by oneTimeMutableInline()

    var unkA: Int by oneTimeMutableInline()
    var unkB: Int by oneTimeMutableInline()
    var unkC: Int by oneTimeMutableInline()
    var vertxSizeDataCount: Int by oneTimeMutableInline()

    var unknownOffset: Int by oneTimeMutableInline()
    var vertexSizeDataOffset: Int by oneTimeMutableInline()
    var floatListOffset: Int by oneTimeMutableInline()
    var bindBoneListOffset: Int by oneTimeMutableInline()
    var unk6: Int by oneTimeMutableInline()
    var unk7: Int by oneTimeMutableInline()

    var shortList: IntArray by oneTimeMutableInline()
    var floatList: FloatArray by oneTimeMutableInline()
    var bindBoneListStringOffsets: IntArray by oneTimeMutableInline()

    var vertexSizeData: Array<VertexSizePair> by oneTimeMutableInline()

    var bindBoneRoot: Int by oneTimeMutableInline()

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup(): KorneaResult<VTXSrdEntry> {
        rsiEntry = RSISrdEntry(this, openSubDataSource()).get()

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow().filterToInstance<SeekableInputFlow>().useAndFlatMap { flow -> setup(flow) }
        else {
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(BinaryInputFlow(flow.readBytes())) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<VTXSrdEntry> {
        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        unk1 = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk2 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk3 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        vertexCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

//        unk4 = requireNotNull(flow.readInt16LE())
//        unk5 = requireNotNull(flow.readInt16LE())

        unkA = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unkB = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unkC = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        vertxSizeDataCount = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        unknownOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        vertexSizeDataOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        floatListOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        bindBoneListOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        unk6 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk7 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        vertexSizeData = flow.fauxSeekFromStart(vertexSizeDataOffset.toULong(), dataSource) { vertexFlow ->
            Array(vertxSizeDataCount) {
                VertexSizePair(requireNotNull(vertexFlow.readInt32LE()), requireNotNull(vertexFlow.readInt32LE()))
            }
        }.getOrBreak { error ->
            return error.cast()
        }

//        shortList = IntArray((endOfShortListOffset - 0x20) / 0x2) { requireNotNull(flow.readInt16LE()) }

//        bindBoneRoot = requireNotNull(flow.readInt16LE())

//        if (bindBoneListOffset != 0 && unk6 != 0) {
//            bindBoneListStringOffsets = requireNotNull(flow.fauxSeekFromStart(bindBoneListOffset.toULong(), dataSource) { boneFlow ->
//                val array = IntArray(unk6 and 0xFF00) { requireNotNull(boneFlow.readInt16LE()) }
//                IntArray(array.size) { i -> array[i] - array[0] }
//            })
//        } else {
//            bindBoneListStringOffsets = IntArray(0)
//        }

        floatList = flow.fauxSeekFromStart(floatListOffset.toULong(), dataSource) { floatFlow ->
            FloatArray(unk1) { requireNotNull(floatFlow.readHalfFloatLE()) }
        }.getOrBreak { error ->
            return error.cast()
        }

//        bindBoneListOffset =

//        floatListOffset =

        //There's more data after this, I'm not gonna try that yet. Seems to be pairs of shorts?

        return KorneaResult.success(this@VTXSrdEntry)
    }
}

private suspend fun InputFlow.readHalfFloatLE(): Float? {
    val hbits = readInt16LE() ?: return null
    var mant: Int = hbits and 0x03ff // 10 bits mantissa

    var exp: Int = hbits and 0x7c00 // 5 bits exponent

    if (exp == 0x7c00) // NaN/Inf
        exp = 0x3fc00 // -> NaN/Inf
    else if (exp != 0) // normalized value
    {
        exp += 0x1c000 // exp - 15 + 127
        if (mant == 0 && exp > 0x1c400) // smooth transition
            return Float.fromBits(hbits and 0x8000 shl 16 or (exp shl 13) or 0x3ff)
    } else if (mant != 0) // && exp==0 -> subnormal
    {
        exp = 0x1c400 // make it normal
        do {
            mant = mant shl 1 // mantissa * 2
            exp -= 0x400 // decrease exp by 1
        } while (mant and 0x400 == 0) // while not normal
        mant = mant and 0x3ff // discard subnormal bit
    } // else +/-0 -> +/-0

    return Float.fromBits(// combine all parts
            hbits and 0x8000 shl 16 // sign  << ( 31 - 15 )
                    or (exp or mant) shl 13) // value << ( 23 - 10 )
}