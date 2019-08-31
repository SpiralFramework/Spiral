package info.spiralframework.formats.images

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt16LE
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readInt64LE
import info.spiralframework.formats.utils.*
import java.io.InputStream

/**
 * Font Map for Danganronpa 1 and 2
 */
class FontMap private constructor(context: SpiralContext, val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x453704674

        operator fun invoke(dataSource: DataSource): FontMap? {
            try {
                return FontMap(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.font_map.invalid", dataSource, iae)

                return null
            }
        }

        fun unsafe(dataSource: DataSource): FontMap = FontMap(dataSource)
    }

    val entryCount: Int
    val start: Int
    val chunkCount: Int
    val startPosition: Int

    val unk1: Int
    val unk2: Int

    val glyphs: Array<FontGlyph>

    init {
        with(context) {
            val stream = dataSource()

            try {
                val magicNum = stream.readInt64LE()

                require(magicNum == MAGIC_NUMBER) { localise("formats.font_map.invalid_magic", "0x${magicNum.toString(16)}", "0x${MAGIC_NUMBER.toString(16)}") }

                entryCount = stream.readInt32LE()
                start = stream.readInt32LE()
                chunkCount = stream.readInt32LE()
                startPosition = stream.readInt32LE()

                unk1 = stream.readInt32LE()
                unk2 = stream.readInt32LE()

                val characters: MutableMap<Int, Char> = HashMap()

                for (i in 0 until chunkCount) {
                    val glyphIndex = stream.readInt16LE()

                    if (glyphIndex > 0 && glyphIndex < Short.MAX_VALUE)
                        characters[glyphIndex] = i.toChar()
                }

                glyphs = Array(entryCount) {
                    val charVal = stream.readInt16LE()
                    val x = stream.readInt16LE()
                    val y = stream.readInt16LE()
                    val width = stream.readInt16LE()
                    val height = stream.readInt16LE()

                    val unk1 = stream.readInt16LE()
                    val unk2 = stream.readInt16LE()
                    val unk3 = stream.readInt16LE()

                    FontGlyph(charVal.toChar(), x, y, width, height, unk1, unk2, unk3, this@FontMap)
                }

                glyphs.sortBy { glyph -> glyph.character }
            } finally {
                stream.close()
            }
        }
    }

    infix fun glyphFor(char: Char): FontGlyph? = glyphs.firstOrNull { glyph -> glyph.character == char }
}