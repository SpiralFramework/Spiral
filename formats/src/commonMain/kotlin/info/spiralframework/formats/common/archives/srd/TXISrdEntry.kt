package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedUTF8String
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue
import info.spiralframework.base.common.useAndFlatMap
import info.spiralframework.base.common.useAndMap
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.bookmark
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

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

    var textureNames: Array<String> by oneTimeMutable()
    var rsiEntry: RSISrdEntry by oneTimeMutable()

    val fileID: String
        get() = rsiEntry.name

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup(): KorneaResult<TXISrdEntry> {
        rsiEntry = RSISrdEntry(this, openSubDataSource()).get()

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(flow) }
        else {
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(BinaryInputFlow(flow.readBytes())) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow): KorneaResult<TXISrdEntry> {
        flow.seek(0, InputFlow.FROM_BEGINNING) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        val textureCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val textureNameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        flow.seek(textureNameOffset.toLong(), InputFlow.FROM_BEGINNING)
        textureNames = Array(textureCount) {
            val nameOffset = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            bookmark(flow) {
                flow.seek(nameOffset.toLong(), InputFlow.FROM_BEGINNING)
                flow.readNullTerminatedUTF8String()
            }
        }

        return KorneaResult.Success(this@TXISrdEntry)
    }
}