package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext

@Suppress("NOTHING_TO_INLINE")
@SrdBuilder
public class CustomSrdArchive {
    public val entries: MutableList<BaseSrdEntry> = ArrayList()

    public inline fun cfh(): Boolean = entries.add(CFHSrdEntry(CFHSrdEntry.MAGIC_NUMBER_BE, 0uL, 0uL, 1))
    public inline fun ct0(): Boolean = entries.add(CT0SrdEntry(CT0SrdEntry.MAGIC_NUMBER_BE, 0uL, 0uL, 0))

    @SrdBuilder
    public inline fun textureEntry(unk1: Int, swizzle: Int, displayWidth: Int, displayHeight: Int, scanline: Int, format: Int, unk2: Int, palette: Int, paletteID: Int, rsi: () -> RSISrdEntry): Boolean =
        entries.add(buildTextureSrdEntry(unk1, swizzle, displayWidth, displayHeight, scanline, format, unk2, palette, paletteID, rsi))

    @SrdBuilder
    public inline fun textureEntry(unk1: Int, swizzle: Int, displayWidth: Int, displayHeight: Int, scanline: Int, format: Int, unk2: Int, palette: Int, paletteID: Int, rsi: RSISrdEntry): Boolean =
        entries.add(buildTextureSrdEntry(unk1, swizzle, displayWidth, displayHeight, scanline, format, unk2, palette, paletteID, rsi))

    @SrdBuilder
    public inline fun rsiEntry(unk1: Int, unk2: Int, unk3: Int, unk4: Int, unk5: Int, unk6: Int, unk7: Int, name: String, block: RsiSrdEntryBuilder.() -> Unit): Boolean =
        entries.add(buildRsiEntry(unk1, unk2, unk3, unk4, unk5, unk6, unk7, name, block))

    public suspend fun compile(context: SpiralContext, output: OutputFlow) {
        entries.forEach { it.writeTo(context, output) }
    }
}

@DslMarker
public annotation class SrdBuilder

@SrdBuilder
public inline fun buildSrdArchive(block: CustomSrdArchive.() -> Unit): CustomSrdArchive {
    val srd = CustomSrdArchive()
    srd.block()
    return srd
}

@SrdBuilder
public suspend inline fun OutputFlow.compileSrdArchive(context: SpiralContext, block: CustomSrdArchive.() -> Unit) {
    val srd = CustomSrdArchive()
    srd.block()
    srd.compile(context, this)
}