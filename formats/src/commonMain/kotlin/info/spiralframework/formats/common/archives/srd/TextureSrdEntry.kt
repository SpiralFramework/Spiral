package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.oneTimeMutableInline

@ExperimentalUnsignedTypes
data class TextureSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : SrdEntryWithData.WithRsiSubdata(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24545852
    }

    val mipmaps: Array<RSISrdEntry.ResourceIndex>
        get() = rsiEntry.resources

    var unk1: Int by oneTimeMutableInline()
    var swizzle: Int by oneTimeMutableInline()
    var displayWidth: Int by oneTimeMutableInline()
    var displayHeight: Int by oneTimeMutableInline()
    var scanline: Int by oneTimeMutableInline()
    var format: Int by oneTimeMutableInline()
    var unk2: Int by oneTimeMutableInline()
    var palette: Int by oneTimeMutableInline()
    var paletteID: Int by oneTimeMutableInline()

    override suspend fun <T> SpiralContext.setup(flow: T): KorneaResult<TextureSrdEntry> where T: InputFlowState<SeekableInputFlow>, T: IntFlowState {
        flow.seek(0, EnumSeekMode.FROM_BEGINNING)

        unk1 = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        swizzle = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        displayWidth = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        displayHeight = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        scanline = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        format = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        unk2 = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        palette = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        paletteID = flow.read()?.and(0xFF) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        return KorneaResult.success(this@TextureSrdEntry)
    }
}