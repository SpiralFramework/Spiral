package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.SpiralContext

@SrdBuilder
class CustomSrdArchive {
    val entries: MutableList<BaseSrdEntry> = ArrayList()

    inline fun cfh() = entries.add(CFHSrdEntry(CFHSrdEntry.MAGIC_NUMBER_BE, 0uL, 0uL, 1))
    inline fun ct0() = entries.add(CT0SrdEntry(CT0SrdEntry.MAGIC_NUMBER_BE, 0uL, 0uL, 0))

    @SrdBuilder
    inline fun textureEntry(unk1: Int, swizzle: Int, displayWidth: Int, displayHeight: Int, scanline: Int, format: Int, unk2: Int, palette: Int, paletteID: Int, rsi: () -> RSISrdEntry) =
        entries.add(buildTextureSrdEntry(unk1, swizzle, displayWidth, displayHeight, scanline, format, unk2, palette, paletteID, rsi))

    @SrdBuilder
    inline fun textureEntry(unk1: Int, swizzle: Int, displayWidth: Int, displayHeight: Int, scanline: Int, format: Int, unk2: Int, palette: Int, paletteID: Int, rsi: RSISrdEntry) =
        entries.add(buildTextureSrdEntry(unk1, swizzle, displayWidth, displayHeight, scanline, format, unk2, palette, paletteID, rsi))

    @SrdBuilder
    inline fun rsiEntry(unk1: Int, unk2: Int, unk3: Int, unk4: Int, unk5: Int, unk6: Int, unk7: Int, name: String, block: RsiSrdEntryBuilder.() -> Unit) =
        entries.add(buildRsiEntry(unk1, unk2, unk3, unk4, unk5, unk6, unk7, name, block))

    suspend fun compile(context: SpiralContext, output: OutputFlow) {
        entries.forEach { it.writeTo(context, output) }
    }
}

@DslMarker
annotation class SrdBuilder

@ExperimentalUnsignedTypes
@SrdBuilder
inline fun buildSrdArchive(block: CustomSrdArchive.() -> Unit): CustomSrdArchive {
    val srd = CustomSrdArchive()
    srd.block()
    return srd
}

@ExperimentalUnsignedTypes
@SrdBuilder
suspend inline fun OutputFlow.compileSrdArchive(context: SpiralContext, block: CustomSrdArchive.() -> Unit) {
    val srd = CustomSrdArchive()
    srd.block()
    srd.compile(context, this)
}