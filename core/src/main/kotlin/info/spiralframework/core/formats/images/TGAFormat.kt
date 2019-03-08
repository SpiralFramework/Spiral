package info.spiralframework.core.formats.images

import info.spiralframework.base.util.locale
import info.spiralframework.core.SpiralCoreData
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.FormatWriteResponse
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.core.use
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import net.npe.tga.TGAReader
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.OutputStream
import javax.imageio.ImageIO

object TGAFormat: ReadableSpiralFormat<BufferedImage>, WritableSpiralFormat {
    override val name: String = "tga"

    override fun preferredConversionFormat(): WritableSpiralFormat? = PNGFormat

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
        try {
            source.use { stream -> return FormatResult.Success(this, TGAReader.readImage(stream.readBytes()), 1.0) }
        } catch (io: IOException) {
            SpiralCoreData.LOGGER.debug("core.formats.tga.invalid", source, io)

            return FormatResult.Fail(this, 1.0, io)
        } catch (iae: IllegalArgumentException) {
            SpiralCoreData.LOGGER.debug("core.formats.tga.invalid", source, iae)

            return FormatResult.Fail(this, 1.0, iae)
        }
    }

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(data: Any): Boolean = data is Image

    /**
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): FormatWriteResponse {
        if (data !is Image)
            return FormatWriteResponse.WRONG_FORMAT

        val width = data.getWidth(null)
        val height = data.getHeight(null)

        if (width < 0 || height < 0)
            return FormatWriteResponse.FAIL(locale<IllegalArgumentException>("core.formats.img.invalid_dimensions", width, height))

        val tga = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = tga.graphics
        try {
            g.drawImage(data, 0, 0, null)
        } finally {
            g.dispose()
        }

        try {
            ImageIO.write(tga, "TGA", stream)

            return FormatWriteResponse.SUCCESS
        } catch (io: IOException) {
            return FormatWriteResponse.FAIL(io)
        }
    }
}