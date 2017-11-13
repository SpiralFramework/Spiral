package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.images.GXTBaseFormat
import org.abimon.spiral.core.objects.images.GXTByteColourOrder
import org.abimon.spiral.core.objects.images.GXTTexture
import org.abimon.spiral.core.readShort
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.spiral.core.swizzle
import org.abimon.spiral.util.SeekableInputStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import java.awt.image.BufferedImage

object GXTFormat: SpiralImageFormat {
    override val name: String = "GXT"
    override val extension: String? = "gxt"
    override val conversions: Array<SpiralFormat> = arrayOf(PNGFormat, JPEGFormat, TGAFormat, SHTXFormat, DDSFormat)
    val HEADER = byteArrayOf(0x47, 0x58, 0x54, 0x00)

    val LINEAR_TEXTURE = 0x60

    override fun isFormat(source: DataSource): Boolean = source.use { stream -> stream.read(4) contentEquals HEADER }

    override fun toBufferedImage(source: DataSource): BufferedImage = source.seekableUse { raw ->
        val stream = SeekableInputStream(raw)
        val magic = stream.read(4)
        val version = stream.read(4)
        val numTextures = stream.readUnsignedLittleInt()
        val headerSize = stream.readUnsignedLittleInt()
        val totalTextureSize = stream.readUnsignedLittleInt()
        val numP4Palettes = stream.readUnsignedLittleInt()
        val numP8Palettes = stream.readUnsignedLittleInt()
        stream.skip(4) //Padding

        val textures = Array<GXTTexture>(numTextures.toInt()) {
            val textureOffset = stream.readUnsignedLittleInt()
            val textureSize = stream.readUnsignedLittleInt()
            val paletteIndex = stream.readUnsignedLittleInt()
            val textureFlags = stream.readUnsignedLittleInt()
            val textureType = stream.readUnsignedLittleInt() shr 24
            val textureBaseFormat = stream.readUnsignedLittleInt()
            val width = stream.readShort(true, true)
            val height = stream.readShort(true, true)
            val mipmaps = stream.readShort(true, true)
            stream.skip(2)

            val baseFormat = GXTBaseFormat[textureBaseFormat]!!
            val colourOrdering = GXTByteColourOrder[baseFormat.hex, textureBaseFormat]!!

            return@Array GXTTexture(
                    textureOffset, textureSize, paletteIndex,
                    textureFlags, textureType, baseFormat, colourOrdering,
                    width, height, mipmaps
            )
        }

        val paletteOffset = headerSize + totalTextureSize - (numP8Palettes * 256 * 4 + numP4Palettes * 16 * 4)

        stream.seek(paletteOffset)

        val p4Palettes = Array<Array<IntArray>>(numP4Palettes.toInt()) { Array<IntArray>(16) { intArrayOf(stream.read(), stream.read(), stream.read(), stream.read()) } }
        val p8Palettes = Array<Array<IntArray>>(numP8Palettes.toInt()) { Array<IntArray>(256) { intArrayOf(stream.read(), stream.read(), stream.read(), stream.read()) } }

        val texture = textures[0]
        val img = BufferedImage(texture.width, texture.height, BufferedImage.TYPE_INT_ARGB)

        stream.seek(texture.textureOffset)

        when(texture.textureBaseFormat) {
            GXTBaseFormat.P4 -> TODO()
            GXTBaseFormat.P8 -> {
                for(y in 0 until texture.height)
                    for(x in 0 until texture.width)
                        img.setRGB(x, y, p8Palettes[texture.paletteIndex.toInt()][stream.read()].swizzle(texture.textureColouring))
            }
        }

        return@seekableUse img
    }
}