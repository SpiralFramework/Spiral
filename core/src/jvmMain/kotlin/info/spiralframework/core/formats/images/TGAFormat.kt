package info.spiralframework.core.formats.images

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.core.use
import info.spiralframework.formats.utils.DataSource
import net.npe.tga.TGAReader
import net.npe.tga.readImage
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.OutputStream
import javax.imageio.ImageIO

object TGAFormat: ReadableSpiralFormat<BufferedImage>, WritableSpiralFormat {
    override val name: String = "tga"
    override val extension: String = "tga"

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
    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<BufferedImage> {
        with(context) {
            try {
                source.use { stream -> return FormatResult.Success(this@TGAFormat, TGAReader.readImage(this, stream.readBytes()), 1.0) }
            } catch (io: IOException) {
                debug("core.formats.tga.invalid", source, io)

                return FormatResult.Fail(this@TGAFormat, 1.0, io)
            } catch (iae: IllegalArgumentException) {
                debug("core.formats.tga.invalid", source, iae)

                return FormatResult.Fail(this@TGAFormat, 1.0, iae)
            }
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
    override fun supportsWriting(context: SpiralContext, data: Any): Boolean = data is Image

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
    override fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, stream: OutputStream): FormatWriteResponse {
        with(context) {
            if (data !is Image)
                return FormatWriteResponse.WRONG_FORMAT

            val width = data.getWidth(null)
            val height = data.getHeight(null)

            if (width < 0 || height < 0)
                return FormatWriteResponse.FAIL(IllegalArgumentException(localise("core.formats.img.invalid_dimensions", width, height)))

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
}