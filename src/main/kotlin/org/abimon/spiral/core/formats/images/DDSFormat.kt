package org.abimon.spiral.core.formats.images

import org.abimon.karnage.raw.DXT1PixelData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.readUnsignedLittleInt
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import org.abimon.visi.io.skipBytes
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream

object DDSFormat : SpiralImageFormat {
    override val name: String = "DDS Texture"
    override val extension: String = ".dds"
    override val conversions: Array<SpiralFormat> = arrayOf(PNGFormat, TGAFormat, SHTXFormat, JPEGFormat)

    override fun isFormat(source: DataSource): Boolean = source.use { it.readString(8) == "DDS1DDS " }

    override fun toBufferedImage(source: DataSource): BufferedImage = source.use { stream ->
        val header = stream.read(132)
        val his = ByteArrayInputStream(header)

        val magic = his.readString(8)

        if (magic != "DDS1DDS ")
            throw IllegalArgumentException("\"$magic\" ≠ DDS1DDS ")

        his.readUnsignedLittleInt() //Size
        his.readUnsignedLittleInt() //Flags
        val height = his.readUnsignedLittleInt().toInt()
        val width = his.readUnsignedLittleInt().toInt()

        //Check the type here or something

        his.skipBytes(104)
        his.readUnsignedLittleInt() //caps2

        return@use DXT1PixelData.read(width, height, stream)
    }
}