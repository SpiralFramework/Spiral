package info.spiralframework.core.formats.images

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

open class SpiralImageIOFormat(vararg val names: String): SpiralImageFormat, ReadableSpiralFormat<BufferedImage> {
    override val name: String = names.firstOrNull() ?: this::class.java.name
    override val extension: String? = null

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<BufferedImage> {
        source().use { stream ->
            val imageStream = ImageIO.createImageInputStream(stream)
            val reader = ImageIO.getImageReaders(imageStream)
                    .asSequence()
                    .firstOrNull { reader -> names.any { name -> name.equals(reader.formatName, true) } }

            try {
                reader?.input = imageStream
                val img = reader?.read(0)

                return FormatResult(this, img, img != null, 1.0)
            } catch (io: IOException) {
                return FormatResult.Fail(this, 1.0, io)
            } finally {
                reader?.dispose()
            }
        }
    }
}