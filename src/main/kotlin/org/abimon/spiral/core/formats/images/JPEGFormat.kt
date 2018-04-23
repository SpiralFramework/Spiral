package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

object JPEGFormat : SpiralImageFormat {
    override val name = "JPEG"
    override val extension = "jpg"
    override val conversions: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, JPEGFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean =
            dataSource().use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { (it.formatName.toLowerCase() == "jpg") or (it.formatName.toLowerCase() == "jpeg") }
            }

    override fun toBufferedImage(name: String?, dataSource: () -> InputStream): BufferedImage = dataSource().use { stream -> ImageIO.read(stream) }
}