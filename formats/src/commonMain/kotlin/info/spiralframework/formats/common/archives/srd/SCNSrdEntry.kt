package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.bookmark
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readNullTerminatedUTF8String
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData

@ExperimentalUnsignedTypes
data class SCNSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : SrdEntryWithData(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x2453434E
    }

//    override suspend fun setup(context: SpiralContext): KorneaResult<BaseSrdEntry> =
//        KorneaResult.success(this)

    var unk: Int by oneTimeMutableInline()

    var sceneRootNodes: Array<String> by oneTimeMutableInline()
    var unknownStrings: Array<String> by oneTimeMutableInline()

    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<SCNSrdEntry> {
        unk = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        val sceneRootNodeIndexOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val sceneRootNodeIndexCount = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val unknownStringIndexOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val unknownStringIndexCount = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        flow.seek(sceneRootNodeIndexOffset.toLong(), EnumSeekMode.FROM_BEGINNING)

        sceneRootNodes = Array(sceneRootNodeIndexCount) {
            val stringOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            bookmark(flow) {
                flow.seek(stringOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                flow.readNullTerminatedUTF8String()
            }
        }

        flow.seek(unknownStringIndexOffset.toLong(), EnumSeekMode.FROM_BEGINNING)

        unknownStrings = Array(unknownStringIndexCount) {
            val stringOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            bookmark(flow) {
                flow.seek(stringOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                flow.readNullTerminatedUTF8String()
            }
        }

        return KorneaResult.success(this@SCNSrdEntry)
    }
}