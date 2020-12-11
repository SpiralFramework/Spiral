package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readFloat32LE
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData

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
) : SrdEntryWithData.WithRsiSubdata(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24565458
    }

    data class VertexSizePair(val offset: Int, val size: Int)

    val vertexBlock: VertexBlock
        get() = rsiEntry.resources[0]
    val faceBlock: FaceBlock
        get() = rsiEntry.resources[1]

    var vectorCount: Int by oneTimeMutableInline()
    var unk2: Int by oneTimeMutableInline()
    var unk3: Int by oneTimeMutableInline()

    var vertexCount: Int by oneTimeMutableInline()

    var unkA: Int by oneTimeMutableInline()
    var unkB: Int by oneTimeMutableInline()
    var unkC: Int by oneTimeMutableInline()
    var vertexSubBlockCount: Int by oneTimeMutableInline()

    var bindBoneRootOffset: Int by oneTimeMutableInline()
    var vertexSubBlockListOffset: Int by oneTimeMutableInline()
    var floatListOffset: Int by oneTimeMutableInline()
    var bindBoneListOffset: Int by oneTimeMutableInline()
    var unk6: Int by oneTimeMutableInline()
    var unk7: Int by oneTimeMutableInline()

    var shortList: IntArray by oneTimeMutableInline()
    var floatList: Array<Triple<Float, Float, Float>> by oneTimeMutableInline()
    var bindBoneListStringOffsets: IntArray by oneTimeMutableInline()

    var vertexSizeData: Array<VertexSizePair> by oneTimeMutableInline()

    var bindBoneRoot: Int by oneTimeMutableInline()

    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<VTXSrdEntry> {
        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        vectorCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk2 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk3 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        vertexCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

//        unk4 = requireNotNull(flow.readInt16LE())
//        unk5 = requireNotNull(flow.readInt16LE())

        unkA = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unkB = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unkC = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        vertexSubBlockCount = flow.read() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        bindBoneRootOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        vertexSubBlockListOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        floatListOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        bindBoneListOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        unk6 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk7 = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        flow.seek(vertexSubBlockListOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
        debug("Reading vertex data @ {0}", flow.globalOffset())

        vertexSizeData = Array(vertexSubBlockCount) {
            VertexSizePair(requireNotNull(flow.readInt32LE()), requireNotNull(flow.readInt32LE()))
        }

//        shortList = IntArray((endOfShortListOffset - 0x20) / 0x2) { requireNotNull(flow.readInt16LE()) }

        flow.seek(bindBoneListOffset.toLong(), EnumSeekMode.FROM_BEGINNING)

            bindBoneRoot = requireNotNull(flow.readInt16LE())

//        if (bindBoneListOffset != 0 && unk6 != 0) {
//            bindBoneListStringOffsets = requireNotNull(flow.fauxSeekFromStart(bindBoneListOffset.toULong(), dataSource) { boneFlow ->
//                val array = IntArray(unk6 and 0xFF00) { requireNotNull(boneFlow.readInt16LE()) }
//                IntArray(array.size) { i -> array[i] - array[0] }
//            })
//        } else {
//            bindBoneListStringOffsets = IntArray(0)
//        }

        flow.seek(floatListOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
        floatList = Array(vectorCount / 2) { Triple(requireNotNull(flow.readFloat32LE()), requireNotNull(flow.readFloat32LE()), requireNotNull(flow.readFloat32LE())) }

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
                or (exp or mant) shl 13
    ) // value << ( 23 - 10 )
}