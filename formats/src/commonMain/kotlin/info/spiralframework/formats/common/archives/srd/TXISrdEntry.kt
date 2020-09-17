package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.locale.localisedNotEnoughData
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.oneTimeMutableInline

@ExperimentalUnsignedTypes
/** Texture Information? */
data class TXISrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24545849
    }

    var textureNames: Array<String> by oneTimeMutableInline()
    var rsiEntry: RSISrdEntry by oneTimeMutableInline()

    val fileID: String
        get() = rsiEntry.name

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup(): KorneaResult<TXISrdEntry> {
        rsiEntry = RSISrdEntry(this, openSubDataSource()).get()

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow()
                .filterToInstance<InputFlow, SeekableInputFlow> { flow -> KorneaResult.success(BinaryInputFlow(flow.readAndClose())) }
                .useFlatMapWithState { flow -> setup(int(flow)) }
        else {
            return dataSource
                .openInputFlow()
                .useFlatMapWithState { flow -> setup(int(BinaryInputFlow(flow.readAndClose()))) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun <T> SpiralContext.setup(flow: T): KorneaResult<TXISrdEntry> where T: InputFlowState<SeekableInputFlow>, T: Int32FlowState {
        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        val textureCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val textureNameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        flow.seek(textureNameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
        textureNames = Array(textureCount) {
            val nameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            bookmark(flow) {
                flow.seek(nameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                flow.readNullTerminatedUTF8String()
            }
        }

        return KorneaResult.success(this@TXISrdEntry)
    }
}