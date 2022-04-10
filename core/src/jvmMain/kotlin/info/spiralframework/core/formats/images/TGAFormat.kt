package info.spiralframework.core.formats.images

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.img.RgbMatrix
import dev.brella.kornea.img.readTargaImage
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.useInputFlowForResult
import dev.brella.kornea.io.jvm.asOutputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.errorAsLocalisedIllegalArgument
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormatReturnResult
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.spiralWrongFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

public object TGAFormat : ReadableSpiralFormat<RgbMatrix>, WritableSpiralFormat<BufferedImage> {
    override val name: String = "tga"
    override val extension: String = "tga"

    override fun preferredConversionFormat(
        context: SpiralContext,
        properties: SpiralProperties?
    ): WritableSpiralFormat<*> = PNGFormat

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<RgbMatrix> {
        with(context) {
            try {
                return source.useInputFlowForResult { flow -> flow.readTargaImage() }
                    .ensureFormatSuccess(1.0)
            } catch (io: IOException) {
                debug("core.formats.tga.invalid", source, io)

                return KorneaResult.thrown(io)
            } catch (iae: IllegalArgumentException) {
                debug("core.formats.tga.invalid", source, iae)

                return KorneaResult.thrown(iae)
            } catch (oob: ArrayIndexOutOfBoundsException) {
                debug("core.formats.tga.invalid", source, oob)

                return KorneaResult.thrown(oob)
            }
        }
    }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is Image || data is RgbMatrix

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<BufferedImage> {
        with(context) {
            val tga: BufferedImage

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

                    tga = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

                    val g = tga.graphics
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

                    tga = BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB)
                    tga.setRGB(0, 0, data.width, data.height, data.rgb, 0, data.width)
                }

                else -> return KorneaResult.spiralWrongFormat()
            }

            try {
                withContext(Dispatchers.IO) {
                    asOutputStream(flow, false) { out ->
                        //TODO: This code is bad!! Write your own goddamn targa writer brella
                        ImageIO.write(tga, "TGA", out)
                    }
                }

                return KorneaResult.success(tga)
            } catch (io: IOException) {
                return KorneaResult.thrown(io)
            }
        }
    }
}