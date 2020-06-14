package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.locale.localisedNotEnoughData
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.filterToInstance
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.SeekableInputFlow
import org.abimon.kornea.io.common.flow.bookmark
import org.abimon.kornea.io.common.flow.readBytes
import org.kornea.toolkit.common.oneTimeMutableInline

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
            return dataSource.openInputFlow().filterToInstance<SeekableInputFlow>().useAndFlatMap { flow -> setup(flow) }
        else {
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(BinaryInputFlow(flow.readBytes())) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<MeshSrdEntry> {
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