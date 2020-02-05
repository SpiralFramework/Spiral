package info.spiralframework.formats.common.images

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.fauxSeekFromStart

@ExperimentalUnsignedTypes
class FontMap(val unk1: Int, val unk2: Int, val mappingTable: Map<Char, Int>, val glyphs: Array<Glyph>) {
    data class Glyph(val character: Char, val x: Int, val y: Int, val width: Int, val height: Int, val unk1: Int, val unk2: Int, val unk3: Int)

    companion object {
        const val PREFIX = "formats.font_map.dr1"
        const val MAGIC_NUMBER_BE = 0x74467053
        val VALID_GLYPH_RANGE = 0 until Short.MAX_VALUE

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): FontMap? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("$PREFIX.invalid", dataSource, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): FontMap {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("$PREFIX.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val magicNumber = requireNotNull(flow.readInt32BE(), notEnoughData)
                    require(magicNumber == MAGIC_NUMBER_BE) { localise("$PREFIX.invalid_magic", "0x${magicNumber.toString(16)}", "0x${MAGIC_NUMBER_BE.toString(16)}") }

                    val version = requireNotNull(flow.readInt32LE(), notEnoughData)

                    trace("$PREFIX.version", version)

                    val fontTableEntryCount = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val fontTableStart = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val mappingTableEntryCount = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val mappingTableStart = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val unk1 = requireNotNull(flow.readInt32LE(), notEnoughData)
                    val unk2 = requireNotNull(flow.readInt32LE(), notEnoughData)

                    val mappingTable: MutableMap<Char, Int> = HashMap()

                    flow.fauxSeekFromStart(mappingTableStart.toULong(), dataSource) { mappingTableFlow ->
                        for (i in 0 until mappingTableEntryCount) {
                            val glyphIndex = requireNotNull(mappingTableFlow.readInt16LE(), notEnoughData)

                            if (glyphIndex in VALID_GLYPH_RANGE) {
                                mappingTable[i.toChar()] = glyphIndex
                            }
                        }
                    }

                    val glyphs = requireNotNull(flow.fauxSeekFromStart(fontTableStart.toULong(), dataSource) { fontTableFlow ->
                        Array(fontTableEntryCount) {
                            val character = requireNotNull(fontTableFlow.readInt16LE()).toChar()
                            val x = requireNotNull(fontTableFlow.readInt16LE())
                            val y = requireNotNull(fontTableFlow.readInt16LE())
                            val width = requireNotNull(fontTableFlow.readInt16LE())
                            val height = requireNotNull(fontTableFlow.readInt16LE())

                            val glyphUnk1 = requireNotNull(fontTableFlow.readInt16LE())
                            val glyphUnk2 = requireNotNull(fontTableFlow.readInt16LE())
                            val glyphUnk3 = requireNotNull(fontTableFlow.readInt16LE())

                            return@Array Glyph(character, x, y, width, height, glyphUnk1, glyphUnk2, glyphUnk3)
                        }
                    })

                    return FontMap(unk1, unk2, mappingTable, glyphs)
                }
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.FontMap(dataSource: DataSource<*>) = FontMap(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeFontMap(dataSource: DataSource<*>) = FontMap.unsafe(this, dataSource)