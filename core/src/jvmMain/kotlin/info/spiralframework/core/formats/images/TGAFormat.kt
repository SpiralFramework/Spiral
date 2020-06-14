package info.spiralframework.core.formats.images

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.npe.tga.TGAReader
import net.npe.tga.readImage
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.getOrElseTransform
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.useInputFlow
import org.abimon.kornea.io.jvm.asOutputStream
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
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<BufferedImage> {
        with(context) {
            try {
                return source.useInputFlow { flow -> FormatResult.Success(this@TGAFormat, TGAReader.readImage(this, flow.readBytes()), 1.0) }
                           .getOrElseTransform { FormatResult.Fail(this@TGAFormat, 1.0, it) }
            } catch (io: IOException) {
                debug("core.formats.tga.invalid", source, io)

                return FormatResult.Fail(this@TGAFormat, 1.0, KorneaResult.WithException.of(io))
            } catch (iae: IllegalArgumentException) {
                debug("core.formats.tga.invalid", source, iae)

                return FormatResult.Fail(this@TGAFormat, 1.0, KorneaResult.WithException.of(iae))
            } catch (oob: ArrayIndexOutOfBoundsException) {
                debug("core.formats.tga.invalid", source, oob)

                return FormatResult.Fail(this@TGAFormat, 1.0, KorneaResult.WithException.of(oob))
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