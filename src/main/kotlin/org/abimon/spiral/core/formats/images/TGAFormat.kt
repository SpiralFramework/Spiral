package org.abimon.spiral.core.formats.images

import net.npe.tga.TGAReader
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream

object TGAFormat : SpiralImageFormat {
    override val name = "TGA"
    override val extension = "tga"
    override val conversions: Array<SpiralFormat> = arrayOf(PNGFormat, JPEGFormat, SHTXFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            dataSource().use { stream -> TGAReader.readImage(stream.readBytes()) }
            return true
        } catch(e: IOException) {
        } catch(e: ArrayIndexOutOfBoundsException) {
        } catch(e: IllegalArgumentException) {
        }
        return false
    }

    override fun toBufferedImage(name: String?, dataSource: () -> InputStream): BufferedImage = dataSource().use { stream -> TGAReader.readImage(stream.readBytes()) }
}