package org.abimon.spiral.core.formats

import org.abimon.spiral.core.toJPG
import org.abimon.spiral.core.toTGA
import org.abimon.visi.io.DataSource
import java.io.OutputStream
import javax.imageio.ImageIO

object PNGFormat : SpiralFormat {
    override val name = "PNG"
    override val extension = "png"

    override fun isFormat(source: DataSource): Boolean =
            source.use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { it.formatName.toLowerCase() == "png" }
            }

    override fun canConvert(format: SpiralFormat): Boolean = (format is TGAFormat) or (format is JPEGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.use {
            when (format) {
                is TGAFormat -> output.write(ImageIO.read(it).toTGA())
                is JPEGFormat -> ImageIO.write(ImageIO.read(it).toJPG(), "JPG", output)
                else -> {
                }
            }
        }
    }
}