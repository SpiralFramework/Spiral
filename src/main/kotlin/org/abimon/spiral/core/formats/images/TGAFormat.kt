package org.abimon.spiral.core.formats.images

import net.npe.tga.TGAReader
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.visi.io.DataSource
import java.awt.image.BufferedImage
import java.io.IOException

object TGAFormat : SpiralImageFormat {
    override val name = "TGA"
    override val extension = "tga"
    override val conversions: Array<SpiralFormat> = arrayOf(PNGFormat, JPEGFormat, SHTXFormat)

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

    override fun toBufferedImage(source: DataSource): BufferedImage = source.use { stream -> TGAReader.readImage(stream.readBytes()) }
}