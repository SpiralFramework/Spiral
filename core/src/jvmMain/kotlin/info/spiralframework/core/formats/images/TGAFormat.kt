package info.spiralframework.core.formats.images

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.npe.tga.TGAReader
import net.npe.tga.readImage
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrElseTransform
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.img.createPngImage
import dev.brella.kornea.img.readTargaImage
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import dev.brella.kornea.io.common.useInputFlowForResult
import dev.brella.kornea.io.jvm.asOutputStream
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

object TGAFormat : ReadableSpiralFormat<BufferedImage>, WritableSpiralFormat {
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
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<BufferedImage> {
        with(context) {
            try {
                //TODO: This code is bad!! Write your own goddamn targa reader brella
                return source.useInputFlowForResult { flow -> flow.readTargaImage() }
                    .map { targa -> targa.createPngImage() }
                    .buildFormatResult(1.0)
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
    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = data is Image

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
    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
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