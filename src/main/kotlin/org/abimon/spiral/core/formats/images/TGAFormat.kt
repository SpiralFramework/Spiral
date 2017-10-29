package org.abimon.spiral.core.formats.images

import net.npe.tga.TGAReader
import org.abimon.spiral.core.byteArrayOfInts
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.toJPG
import org.abimon.spiral.core.writeShort
import org.abimon.visi.io.DataSource
import java.awt.Color
import java.io.IOException
import java.io.OutputStream
import javax.imageio.ImageIO

object TGAFormat : SpiralFormat {
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

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val img = TGAReader.readImage(source.data)
        when (format) {
            is PNGFormat -> ImageIO.write(img, "PNG", output)
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
            is SHTXFormat -> {
                output.write("SHTX".toByteArray())

                var palette: List<Color> = img.run {
                    val pixels = ArrayList<Color>()

                    for (y in 0 until height)
                        for (x in 0 until width)
                            pixels.add(Color(getRGB(x, y), true))

                    return@run pixels
                }

                if (palette.distinctBy { it.rgb }.size > 256) {
                    output.write("Ff".toByteArray())
                    output.writeShort(img.width)
                    output.writeShort(img.height)
                    output.writeShort(0)

                    palette.forEach { colour -> output.write(byteArrayOfInts(colour.red, colour.green, colour.blue, colour.alpha)) }
                } else {
                    palette = palette.distinctBy { it.rgb }

                    output.write("Fs".toByteArray())
                    output.writeShort(img.width)
                    output.writeShort(img.height)
                    output.writeShort(0)

                    palette.forEach { colour -> output.write(byteArrayOfInts(colour.red, colour.green, colour.blue, colour.alpha)) }

                    img.run {
                        for (y in 0 until height)
                            for (x in 0 until width)
                                output.write(palette.indexOf(Color(getRGB(x, y), true)))
                    }
                }
            }
        }

        return true
    }
}