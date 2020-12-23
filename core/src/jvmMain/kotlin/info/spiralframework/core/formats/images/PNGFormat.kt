package info.spiralframework.core.formats.images

import dev.brella.kornea.img.RgbMatrix
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.jvm.asOutputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.WritableSpiralFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

object PNGFormat: SpiralImageIOFormat("png"), WritableSpiralFormat {
    override val name: String = "png"
    override val extension: String = "png"

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = data is Image || data is RgbMatrix

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
    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        with(context) {
            val png: BufferedImage
            when (data) {
                is Image -> {
                    val width = data.getWidth(null)
                    val height = data.getHeight(null)

                    if (width < 0 || height < 0)
                        return FormatWriteResponse.FAIL(IllegalArgumentException(localise("core.formats.img.invalid_dimensions", width, height)))

                    png = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    val g = png.graphics
                    try {
                        g.drawImage(data, 0, 0, null)
                    } finally {
                        g.dispose()
                    }
                }
                is RgbMatrix -> {
                    if (data.width < 0 || data.height < 0)
                        return FormatWriteResponse.FAIL(IllegalArgumentException(localise("core.formats.img.invalid_dimensions", data.width, data.height)))

                    png = BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB)
                    png.setRGB(0, 0, data.width, data.height, data.rgb, 0, data.width)
                }
                else -> return FormatWriteResponse.WRONG_FORMAT
            }

            try {
                withContext(Dispatchers.IO) {
                    asOutputStream(flow, false) { out ->
                        ImageIO.write(png, "PNG", out)
                    }
                }

                return FormatWriteResponse.SUCCESS
            } catch (io: IOException) {
                return FormatWriteResponse.FAIL(io)
            }
        }
    }
}