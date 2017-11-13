package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.*
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.visi.io.DataSource
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.ImageIO

interface SpiralImageFormat: SpiralFormat {
    override fun canConvert(format: SpiralFormat): Boolean = format is SpiralImageFormat || super.canConvert(format)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params))
            return true

        return convert(format, toBufferedImage(source), output, params)
    }

    fun convert(format: SpiralFormat, img: BufferedImage, output: OutputStream, params: Map<String, Any?>): Boolean {
        when (format) {
            is TGAFormat -> output.write(img.toTGA())
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
            is PNGFormat -> ImageIO.write(img.toPNG(), "PNG", output)
            is SHTXFormat -> {
                output.write("SHTX".toByteArray())

                var palette: List<Color> = img.run {
                    val pixels = ArrayList<Color>()

                    for(y in 0 until height)
                        for(x in 0 until width)
                            pixels.add(Color(getRGB(x, y), true))

                    return@run pixels
                }

                if(palette.distinctBy { it.rgb }.size > 256) {
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
                        for(y in 0 until height)
                            for(x in 0 until width)
                                output.write(palette.indexOf(Color(getRGB(x, y), true)))
                    }
                }
            }
            else -> return false
        }

        return true
    }

    fun toBufferedImage(source: DataSource): BufferedImage
}