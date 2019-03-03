package info.spiralframework.core.formats.images

import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readInt64LE
import org.abimon.karnage.raw.DXT1PixelData
import java.awt.image.BufferedImage
import java.io.InputStream

abstract class DDSImageFormat(override val name: String, val typeMagic: Int): ReadableSpiralFormat<BufferedImage> {
    companion object {
        val MAGIC_NUMBER = 0x2053444431534444
        val DXT1_MAGIC = 0x31545844

        val DXT1 = object: DDSImageFormat("DDS DXT1", DXT1_MAGIC) {
            override fun readImage(width: Int, height: Int, stream: InputStream, ddsType: Int): FormatResult<BufferedImage> =
                    FormatResult.Success(DXT1PixelData.read(width, height, stream), 1.0)
        }
    }

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
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<BufferedImage> {
        val stream = source()

        try {
            val magic = stream.readInt64LE()

            if (magic != MAGIC_NUMBER)
                return FormatResult.Fail(1.0)

            stream.readInt32LE() //Size
            stream.readInt32LE() //Flags
            val height = stream.readInt32LE()
            val width = stream.readInt32LE()

            stream.skip(64)

            val ddsType = stream.readInt32LE()
            stream.skip(36)
            stream.readInt32LE() //caps2

            if (ddsType != typeMagic)
                return FormatResult.Fail(1.0)

            return readImage(width, height, stream, ddsType)
        } finally {
            stream.close()
        }
    }

    abstract fun readImage(width: Int, height: Int, stream: InputStream, ddsType: Int): FormatResult<BufferedImage>
}