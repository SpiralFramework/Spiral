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
data class MeshSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : SrdEntryWithData.WithRsiSubdata(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x244D5348
    }

    var unk: Int by oneTimeMutableInline()
    var meshName: String by oneTimeMutableInline()
    var materialName: String by oneTimeMutableInline()

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<MeshSrdEntry> {
        unk = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        val meshNameOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val materialNameOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        bookmark(flow) {
            flow.seek(meshNameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
            meshName = flow.readNullTerminatedUTF8String()

            flow.seek(materialNameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
            materialName = flow.readNullTerminatedUTF8String()
        }

        return KorneaResult.success(this@MeshSrdEntry)
    }
}