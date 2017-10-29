package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.byteArrayOfInts
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.toJPG
import org.abimon.spiral.core.toTGA
import org.abimon.spiral.core.writeShort
import org.abimon.visi.io.DataSource
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.ImageIO

object PNGFormat : SpiralFormat {
    override val name = "PNG"
    override val extension = "png"
    override val conversions: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, JPEGFormat)

    override fun isFormat(source: DataSource): Boolean =
            source.use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { it.formatName.toLowerCase() == "png" }
            }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        source.use { convert(format, ImageIO.read(it), output) }

        return true
    }

    fun convert(to: SpiralFormat, img: BufferedImage, output: OutputStream) {
        when (to) {
            is TGAFormat -> output.write(img.toTGA())
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
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
        }
    }
}