package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

object PNGFormat : SpiralImageFormat {
    override val name = "PNG"
    override val extension = "png"
    override val conversions: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, JPEGFormat)

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean =
            dataSource().use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { it.formatName.toLowerCase() == "png" }
            }

    override fun toBufferedImage(name: String?, dataSource: () -> InputStream): BufferedImage = dataSource().use { stream -> ImageIO.read(stream) }
}