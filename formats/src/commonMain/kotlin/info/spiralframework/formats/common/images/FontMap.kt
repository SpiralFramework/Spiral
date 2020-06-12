package info.spiralframework.formats.common.images

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.erorrs.common.*
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.fauxSeekFromStart

@ExperimentalUnsignedTypes
class FontMap(val unk1: Int, val unk2: Int, val mappingTable: Map<Char, Int>, val glyphs: Array<Glyph>) {
    data class Glyph(val character: Char, val x: Int, val y: Int, val width: Int, val height: Int, val unk1: Int, val unk2: Int, val unk3: Int)

    companion object {
        const val PREFIX = "formats.font_map.dr1"

        const val INVALID_MAGIC = 0x0000

        const val NOT_ENOUGH_DATA_KEY = "$PREFIX.not_enough_data"
        const val INVALID_MAGIC_KEY = "$PREFIX.invalid_magic"

        const val MAGIC_NUMBER_BE = 0x74467053
        val VALID_GLYPH_RANGE = 0 until Short.MAX_VALUE

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<FontMap> {
            withFormats(context) {
                val flow = dataSource.openInputFlow().doOnFailure { return it.cast() }

                use(flow) {
                    val magicNumber = flow.readInt32BE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magicNumber != MAGIC_NUMBER_BE) {
                        return KorneaResult.Error(INVALID_MAGIC, localise(INVALID_MAGIC_KEY, "0x${magicNumber.toString(16)}", "0x${MAGIC_NUMBER_BE.toString(16)}"))
                    }

                    val version = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    trace("$PREFIX.version", version)

                    val fontTableEntryCount = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val fontTableStart = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val mappingTableEntryCount = flow.readInt32LE()
                            ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val mappingTableStart = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val unk1 = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unk2 = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val mappingTable = flow.fauxSeekFromStart(mappingTableStart.toULong(), dataSource) { mappingTableFlow ->
                        val mappingTable: MutableMap<Char, Int> = HashMap()

                        for (i in 0 until mappingTableEntryCount) {
                            val glyphIndex = mappingTableFlow.readInt16LE()
                                    ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                            if (glyphIndex in VALID_GLYPH_RANGE) {
                                mappingTable[i.toChar()] = glyphIndex
                            }
                        }

                        mappingTable
                    }

                    val glyphs = flow.fauxSeekFromStart(fontTableStart.toULong(), dataSource) { fontTableFlow ->
                        Array(fontTableEntryCount) {
                            val character = fontTableFlow.readInt16LE()?.toChar()
                                    ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val x = fontTableFlow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val y = fontTableFlow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val width = fontTableFlow.readInt16LE()
                                    ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val height = fontTableFlow.readInt16LE()
                                    ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                            val glyphUnk1 = fontTableFlow.readInt16LE()
                                    ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val glyphUnk2 = fontTableFlow.readInt16LE()
                                    ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                            val glyphUnk3 = fontTableFlow.readInt16LE()
                                    ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                            return@Array Glyph(character, x, y, width, height, glyphUnk1, glyphUnk2, glyphUnk3)
                        }
                    }

                    @Suppress("NAME_SHADOWING")
                    return mappingTable.flatMap { mappingTable ->
                        glyphs.flatMap { glyphs ->
                            KorneaResult.Success(FontMap(unk1, unk2, mappingTable, glyphs))
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.FontMap(dataSource: DataSource<*>) = FontMap(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeFontMap(dataSource: DataSource<*>) = FontMap(this, dataSource).get()