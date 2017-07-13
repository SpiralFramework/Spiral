package org.abimon.spiral.core.formats

import org.abimon.spiral.core.toTGA
import org.abimon.visi.io.DataSource
import java.io.OutputStream
import javax.imageio.ImageIO

object JPEGFormat : SpiralFormat {
    override val name = "JPEG"
    override val extension = "jpg"

    override fun isFormat(source: DataSource): Boolean =
            source.use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { (it.formatName.toLowerCase() == "jpg") or (it.formatName.toLowerCase() == "jpeg") }
            }

    override fun canConvert(format: SpiralFormat): Boolean = (format is TGAFormat) or (format is PNGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.use {
            when (format) {
                is TGAFormat -> output.write(ImageIO.read(it).toTGA())
                is PNGFormat -> ImageIO.write(ImageIO.read(it), "PNG", output)
                else -> {
                }
            }
        }
    }
}