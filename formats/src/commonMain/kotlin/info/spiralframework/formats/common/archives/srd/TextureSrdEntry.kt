package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
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
import org.abimon.kornea.io.common.flow.readAndClose
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

@ExperimentalUnsignedTypes
data class TextureSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24545852
    }

    var rsiEntry: RSISrdEntry by oneTimeMutable()
    val mipmaps: Array<RSISrdEntry.ResourceIndex>
        get() = rsiEntry.resources

    var unk1: Int by oneTimeMutable()
    var swizzle: Int by oneTimeMutable()
    var displayWidth: Int by oneTimeMutable()
    var displayHeight: Int by oneTimeMutable()
    var scanline: Int by oneTimeMutable()
    var format: Int by oneTimeMutable()
    var unk2: Int by oneTimeMutable()
    var palette: Int by oneTimeMutable()
    var paletteID: Int by oneTimeMutable()
    
    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup(): KorneaResult<TextureSrdEntry> {
        rsiEntry = RSISrdEntry(this, openSubDataSource()).get()

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(flow) }
        else {
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(BinaryInputFlow(flow.readBytes())) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow): KorneaResult<TextureSrdEntry> {
        flow.seek(0, InputFlow.FROM_BEGINNING) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        unk1 = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        swizzle = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        displayWidth = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        displayHeight = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        scanline = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        format = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk2 = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        palette = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        paletteID = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        return KorneaResult.Success(this@TextureSrdEntry)
    }
}