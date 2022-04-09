package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import dev.brella.kornea.io.common.flow.extensions.writeInt32LE
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData

public data class TextureSrdEntry(
    override val classifier: Int,
    override val mainDataLength: ULong,
    override val subDataLength: ULong,
    override val unknown: Int
) : SrdEntryWithData.WithRsiSubdata(classifier, mainDataLength, subDataLength, unknown) {
    public companion object {
        public const val MAGIC_NUMBER_BE: Int = 0x24545852
    }

    val mipmaps: List<RSISrdEntry.ResourceIndex.GlobalTextureResource>
        get() = rsiEntry.resources.filterIsInstance<RSISrdEntry.ResourceIndex.GlobalTextureResource>()

    var unk1: Int by oneTimeMutableInline()
    var swizzle: Int by oneTimeMutableInline()
    var displayWidth: Int by oneTimeMutableInline()
    var displayHeight: Int by oneTimeMutableInline()
    var scanline: Int by oneTimeMutableInline()
    var format: Int by oneTimeMutableInline()
    var unk2: Int by oneTimeMutableInline()
    var palette: Int by oneTimeMutableInline()
    var paletteID: Int by oneTimeMutableInline()

    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<TextureSrdEntry> {
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

    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {
        out.writeInt32LE(unk1)
        out.writeInt16LE(swizzle)
        out.writeInt16LE(displayWidth)
        out.writeInt16LE(displayHeight)
        out.writeInt16LE(scanline)
        out.write(format)
        out.write(unk2)
        out.write(palette)
        out.write(paletteID)
    }

    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {
        rsiEntry.writeTo(this, out)
        CT0SrdEntry(CT0SrdEntry.MAGIC_NUMBER_BE, 0uL, 0uL, 0).writeTo(this, out)
    }
}

@SrdBuilder
public inline fun buildTextureSrdEntry(unk1: Int, swizzle: Int, displayWidth: Int, displayHeight: Int, scanline: Int, format: Int, unk2: Int, palette: Int, paletteID: Int, rsi: () -> RSISrdEntry): TextureSrdEntry =
    buildTextureSrdEntry(unk1, swizzle, displayWidth, displayHeight, scanline, format, unk2, palette, paletteID, rsi())

@Suppress("NOTHING_TO_INLINE")
@SrdBuilder
public inline fun buildTextureSrdEntry(unk1: Int, swizzle: Int, displayWidth: Int, displayHeight: Int, scanline: Int, format: Int, unk2: Int, palette: Int, paletteID: Int, rsi: RSISrdEntry): TextureSrdEntry {
    val entry = TextureSrdEntry(TextureSrdEntry.MAGIC_NUMBER_BE, ULong.MAX_VALUE, ULong.MAX_VALUE, 0)
    entry.unk1 = unk1
    entry.swizzle = swizzle
    entry.displayWidth = displayWidth
    entry.displayHeight = displayHeight
    entry.scanline = scanline
    entry.format = format
    entry.unk2 = unk2
    entry.palette = palette
    entry.paletteID = paletteID

    entry.rsiEntry = rsi

    return entry
}