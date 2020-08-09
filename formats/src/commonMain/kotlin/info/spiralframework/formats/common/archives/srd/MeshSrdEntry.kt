package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.locale.localisedNotEnoughData
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.oneTimeMutableInline

@ExperimentalUnsignedTypes
data class MeshSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x244D5348
    }

    var meshName: String by oneTimeMutableInline()
    var materialName: String by oneTimeMutableInline()

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup(): KorneaResult<MeshSrdEntry> {
        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow().filterToInstance<SeekableInputFlow>().useFlatMapWithState { flow -> setup(int(flow)) }
        else {
            return dataSource.openInputFlow().useFlatMapWithState { flow -> setup(int(BinaryInputFlow(flow.readBytes()))) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun <T> SpiralContext.setup(flow: T): KorneaResult<MeshSrdEntry> where T: InputFlowState<SeekableInputFlow>, T: IntFlowState {
        val unk = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

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