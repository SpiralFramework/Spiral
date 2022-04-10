package info.spiralframework.core.formats.images

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.img.RgbMatrix
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.jvm.asOutputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.errorAsLocalisedIllegalArgument
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.spiralWrongFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

public object JPEGFormat : SpiralImageIOFormat("jpg", "jpeg"), WritableSpiralFormat<BufferedImage> {
    override val name: String = "jpg"
    override val extension: String = "jpg"

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is Image || data is RgbMatrix

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
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<BufferedImage> {
        with(context) {
            val jpg: BufferedImage

            when (data) {
                is Image -> {
                    val width = data.getWidth(null)
                    val height = data.getHeight(null)

                    if (width < 0 || height < 0)
                        return context.errorAsLocalisedIllegalArgument(
                            -1,
                            "core.formats.img.invalid_dimensions",
                            width,
                            height
                        )

                    jpg = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                    val g = jpg.graphics
                    try {
                        g.drawImage(data, 0, 0, null)
                    } finally {
                        g.dispose()
                    }
                }
                is RgbMatrix -> {
                    if (data.width < 0 || data.height < 0)
                        return context.errorAsLocalisedIllegalArgument(
                            -1,
                            "core.formats.img.invalid_dimensions",
                            data.width,
                            data.height
                        )

                    jpg = BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB)
                    jpg.setRGB(0, 0, data.width, data.height, data.rgb, 0, data.width)
                }
                else -> return KorneaResult.spiralWrongFormat()
            }

            try {
                withContext(Dispatchers.IO) {
                    asOutputStream(flow, false) { out ->
                        ImageIO.write(jpg, "JPG", out)
                    }
                }

                return KorneaResult.success(jpg)
            } catch (io: IOException) {
                return KorneaResult.thrown(io)
            }
        }
    }
}