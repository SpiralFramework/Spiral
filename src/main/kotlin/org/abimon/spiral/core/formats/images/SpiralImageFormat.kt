package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.*
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

interface SpiralImageFormat : SpiralFormat {
    override fun canConvert(game: DRGame?, format: SpiralFormat): Boolean = format is SpiralImageFormat || super.canConvert(game, format)

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params))
            return true

        return convert(format, toBufferedImage(name, dataSource), output, params)
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
            is GXTFormat -> {
                //First, we break the texture down into a palette
                val palette = IntArray(256)
                var paletteIndice = 0

                for (y in 0 until img.height)
                    for (x in 0 until img.width) {
                        if (img.getRGB(x, y) !in palette) {
                            //Remainder of 256 just in case we get a bad image. I'll try and smooth it out later
                            palette[paletteIndice % 256] = img.getRGB(x, y)
                            paletteIndice++
                        }
                    }

                output.write(GXTFormat.HEADER)
                output.write(GXTFormat.VERSION)
                output.writeInt(1, true, true)
                output.writeInt(64, true, true)
                output.writeInt((img.width * img.height) + (256 * 4), true, true)
                output.writeInt(0, true, true)
                output.writeInt(1, true, true)
                output.writeInt(0, true, true)

                //Write texture information
                output.writeInt(64, true, true)
                output.writeInt(img.width * img.height, true, true)
                output.writeInt(0, true, true)
                output.writeInt(0, true, true)
                output.write(GXTFormat.LINEAR_TEXTURE)
                output.write(GXTFormat.PALETTE_BGRA)
                output.writeShort(img.width, true, true)
                output.writeShort(img.height, true, true)
                output.writeShort(1, true, true)
                output.writeShort(0, true, true)

                //Now we write indices
                for (y in 0 until img.height)
                    for (x in 0 until img.width) {
                        if (img.getRGB(x, y) !in palette)
                            output.write(255)
                        else
                            output.write(palette.indexOf(img.getRGB(x, y)))
                    }

                //Now we write the palette
                palette.forEach { rgba ->
                    val colour = Color(rgba, true)

                    output.write(colour.blue)
                    output.write(colour.green)
                    output.write(colour.red)
                    output.write(colour.alpha)
                }


            }
            else -> return false
        }

        return true
    }

    fun toBufferedImage(name: String?, dataSource: () -> InputStream): BufferedImage
}