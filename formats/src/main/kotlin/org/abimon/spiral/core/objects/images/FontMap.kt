package org.abimon.spiral.core.objects.images

import org.abimon.spiral.core.utils.*
import java.io.InputStream

/**
 * Font Map for Danganronpa 1 and 2
 */
class FontMap private constructor(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x453704674

        operator fun invoke(dataSource: () -> InputStream): FontMap? {
            try {
                return FontMap(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("Failed to compile FontMap for dataSource {}", dataSource, iae)

                return null
            }
        }
    }

    val entryCount: Int
    val start: Int
    val chunkCount: Int
    val startPosition: Int

    val unk1: Int
    val unk2: Int

    val glyphs: Array<FontGlyph>

    init {
        val stream = dataSource()

        try {
            val magicNum = stream.readInt64LE()

            assertAsArgument(magicNum == MAGIC_NUMBER, "Illegal magic number for FontMap (Was 0x${magicNum.toString(16)}, expected 0x${MAGIC_NUMBER.toString(16)})")

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

                FontGlyph(charVal.toChar(), x, y, width, height, unk1, unk2, unk3, this)
            }

            glyphs.sortBy { glyph -> glyph.character }
        } finally {
            stream.close()
        }
    }

    infix fun glyphFor(char: Char): FontGlyph? = glyphs.firstOrNull { glyph -> glyph.character == char }
}