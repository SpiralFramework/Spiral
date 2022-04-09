package info.spiralframework.core.formats.images

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.useAndFlatMap
import dev.brella.kornea.img.asMatrixFromPng
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.readBytes
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormatBridge
import info.spiralframework.core.common.formats.images.SHTXFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO

open class SpiralImageIOFormat(vararg val names: String) : SpiralImageFormat, ReadableSpiralFormat<BufferedImage> {
    object Bridge: WritableSpiralFormatBridge {
        override fun supportsWritingAs(context: SpiralContext, writeContext: SpiralProperties?, format: WritableSpiralFormat, data: Any): Boolean =
            data is Image && format == SHTXFormat

        override suspend fun writeAs(context: SpiralContext, writeContext: SpiralProperties?, format: WritableSpiralFormat, data: Any, flow: OutputFlow): FormatWriteResponse =
            when (format) {
                is SHTXFormat -> when (data) {
                    is BufferedImage -> {
                        SHTXFormat.write(context, writeContext, data.asMatrixFromPng(), flow)
                        FormatWriteResponse.SUCCESS
                    }
                    is Image -> {
                        SHTXFormat.write(context, writeContext, data.asMatrixFromPng(), flow)
                        FormatWriteResponse.SUCCESS
                    }
                    else -> FormatWriteResponse.WRONG_FORMAT
                }
                else -> FormatWriteResponse.WRONG_FORMAT
            }
    }

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
    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<BufferedImage> =
        source.openInputFlow()
            .useAndFlatMap { flow ->
                val stream = ByteArrayInputStream(flow.readBytes(dataSize = 5_000_000))
                val imageStream = withContext(Dispatchers.IO) { ImageIO.createImageInputStream(stream) }
                val reader = ImageIO.getImageReaders(imageStream)
                    .asSequence()
                    .firstOrNull { reader -> names.any { name -> name.equals(reader.formatName, true) } }

                try {
                    reader?.input = imageStream

                    return@useAndFlatMap KorneaResult.successOrEmpty(withContext(Dispatchers.IO) { reader?.read(0) })
                } catch (io: IOException) {
                    return@useAndFlatMap KorneaResult.WithException.of(io)
                } finally {
                    reader?.dispose()
                }
            }
            .ensureFormatSuccess(1.0)
}