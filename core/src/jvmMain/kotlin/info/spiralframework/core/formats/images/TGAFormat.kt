package info.spiralframework.core.formats.images

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.img.RgbMatrix
import dev.brella.kornea.img.readTargaImage
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.useInputFlowForResult
import dev.brella.kornea.io.jvm.asOutputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

object TGAFormat : ReadableSpiralFormat<RgbMatrix>, WritableSpiralFormat {
    override val name: String = "tga"
    override val extension: String = "tga"

    override fun preferredConversionFormat(context: SpiralContext, properties: SpiralProperties?): WritableSpiralFormat = PNGFormat

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
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<RgbMatrix> {
        with(context) {
            try {
                return source.useInputFlowForResult { flow -> flow.readTargaImage() }
                    .ensureFormatSuccess(1.0)
            } catch (io: IOException) {
                debug("core.formats.tga.invalid", source, io)

                return KorneaResult.WithException.of(io)
            } catch (iae: IllegalArgumentException) {
                debug("core.formats.tga.invalid", source, iae)

                return KorneaResult.WithException.of(iae)
            } catch (oob: ArrayIndexOutOfBoundsException) {
                debug("core.formats.tga.invalid", source, oob)

                return KorneaResult.WithException.of(oob)
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
            val tga: BufferedImage

            when (data) {
                is Image -> {
                    val width = data.getWidth(null)
                    val height = data.getHeight(null)

                    if (width < 0 || height < 0)
                        return FormatWriteResponse.FAIL(IllegalArgumentException(localise("core.formats.img.invalid_dimensions", width, height)))

                    tga = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

                    val g = tga.graphics
                    try {
                        g.drawImage(data, 0, 0, null)
                    } finally {
                        g.dispose()
                    }
                }
                is RgbMatrix -> {
                    tga = BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB)
                    tga.setRGB(0, 0, data.width, data.height, data.rgb, 0, data.width)
                }

                else -> return FormatWriteResponse.WRONG_FORMAT
            }

            try {
                withContext(Dispatchers.IO) {
                    asOutputStream(flow, false) { out ->
                        //TODO: This code is bad!! Write your own goddamn targa writer brella
                        ImageIO.write(tga, "TGA", out)
                    }
                }

                return FormatWriteResponse.SUCCESS
            } catch (io: IOException) {
                return FormatWriteResponse.FAIL(io)
            }
        }
    }
}