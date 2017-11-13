package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.visi.io.DataSource
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object PNGFormat : SpiralImageFormat {
    override val name = "PNG"
    override val extension = "png"
    override val conversions: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, JPEGFormat)

    override fun isFormat(source: DataSource): Boolean =
            source.use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { it.formatName.toLowerCase() == "png" }
            }

    override fun toBufferedImage(source: DataSource): BufferedImage = source.use { stream -> ImageIO.read(stream) }
}