package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.visi.io.DataSource
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object JPEGFormat : SpiralImageFormat {
    override val name = "JPEG"
    override val extension = "jpg"
    override val conversions: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, JPEGFormat)

    override fun isFormat(source: DataSource): Boolean =
            source.use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { (it.formatName.toLowerCase() == "jpg") or (it.formatName.toLowerCase() == "jpeg") }
            }

    override fun toBufferedImage(source: DataSource): BufferedImage = source.use { stream -> ImageIO.read(stream) }
}