package org.abimon.spiral.core.formats

import net.npe.tga.TGAReader
import org.abimon.spiral.core.toJPG
import org.abimon.visi.io.DataSource
import java.io.IOException
import java.io.OutputStream
import javax.imageio.ImageIO

object TGAFormat : SpiralFormat {
    override val name = "TGA"
    override val extension = "tga"
    override val preferredConversions: Array<SpiralFormat> = arrayOf(PNGFormat, JPEGFormat)

    override fun isFormat(source: DataSource): Boolean {
        try {
            TGAReader.readImage(source.data)
            return true
        } catch(e: IOException) {
        } catch(e: ArrayIndexOutOfBoundsException) {
        } catch(e: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = (format is PNGFormat) or (format is JPEGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val img = TGAReader.readImage(source.data)
        when (format) {
            is PNGFormat -> ImageIO.write(img, "PNG", output)
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
        }
    }
}