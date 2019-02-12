package info.spiralframework.core.formats.images

import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.OutputStream
import javax.imageio.ImageIO

object JPEGFormat : SpiralImageIOFormat("jpg", "jpeg"), WritableSpiralFormat {
    override val name: String = "jpg"

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
    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): EnumFormatWriteResponse {
        if (data !is Image)
            return EnumFormatWriteResponse.WRONG_FORMAT

        val width = data.getWidth(null)
        val height = data.getHeight(null)

        if (width == -1 || height == -1)
            return EnumFormatWriteResponse.FAIL

        val jpg = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = jpg.graphics
        try {
            g.drawImage(data, 0, 0, null)
        } finally {
            g.dispose()
        }

        try {
            ImageIO.write(jpg, "JPG", stream)

            return EnumFormatWriteResponse.SUCCESS
        } catch (io: IOException) {
            return EnumFormatWriteResponse.FAIL
        }
    }
}