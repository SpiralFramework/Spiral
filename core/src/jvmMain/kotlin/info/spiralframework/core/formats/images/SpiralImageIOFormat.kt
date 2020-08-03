package info.spiralframework.core.formats.images

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.FormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrElseTransform
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO

open class SpiralImageIOFormat(vararg val names: String) : SpiralImageFormat, ReadableSpiralFormat<BufferedImage> {
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
    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<BufferedImage> =
            source.useInputFlow { flow ->
                val stream = ByteArrayInputStream(flow.readBytes())
                val imageStream = withContext(Dispatchers.IO) { ImageIO.createImageInputStream(stream) }
                val reader = ImageIO.getImageReaders(imageStream)
                        .asSequence()
                        .firstOrNull { reader -> names.any { name -> name.equals(reader.formatName, true) } }

                try {
                    reader?.input = imageStream
                    val img = withContext(Dispatchers.IO) {
                        reader?.read(0)
                    }

                    return@useInputFlow FormatResult(this, img, img != null, 1.0)
                } catch (io: IOException) {
                    return@useInputFlow FormatResult.Fail<BufferedImage>(this, 1.0, KorneaResult.WithException.of(io))
                } finally {
                    reader?.dispose()
                }
            }.getOrElseTransform { FormatResult.Fail(this, 1.0, it) }
}