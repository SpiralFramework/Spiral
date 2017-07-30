package org.abimon.spiral.core.formats

import org.abimon.spiral.core.readNumber
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.toJPG
import org.abimon.spiral.core.toTGA
import org.abimon.visi.collections.coerceAtMost
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readChunked
import org.abimon.visi.io.readPartialBytes
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.OutputStream
import java.util.*
import javax.imageio.ImageIO

object SHTXFormat : SpiralFormat {
    override val name = "SHTX"
    override val extension = null
    override val conversions: Array<SpiralFormat> = arrayOf(PNGFormat, JPEGFormat, TGAFormat)

    override fun isFormat(source: DataSource): Boolean = source.use { it.readString(4) == "SHTX" }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)

        val img = toBufferedImage(source)
        when (format) {
            is PNGFormat -> ImageIO.write(img, "PNG", output)
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
            is TGAFormat -> output.write(img.toTGA())
        }
    }

    /** This information is taken from BlackDragonHunt's Danganronpa-Tools */
    fun toBufferedImage(source: DataSource): BufferedImage {
        //    throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format")

        return source.use { stream ->
            val shtx = stream.readString(4)

            if (shtx != "SHTX")
                throw IllegalArgumentException("${source.location} does not conform to the $name format (First four bytes do not spell [SHTX], spell $shtx)")

            val version = stream.readPartialBytes(2, 2)

            when (String(version)) {
                "Fs" -> {
                    val width = stream.readNumber(2, unsigned = true).toInt()
                    val height = stream.readNumber(2, unsigned = true).toInt()
                    val unknown = stream.readNumber(2, unsigned = true)

                    val palette = ArrayList<Color>()

                    for (i in 0 until 256)
                        palette.add(Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF))

                    if (palette.all { it.red == 0 && it.green == 0 && it.blue == 0 && it.alpha == 0 }) {
                        println("Blank palette in Fs")

                        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                        for (y in 0 until height)
                            for (x in 0 until width)
                                img.setRGB(x, y, Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF).rgb)
                        return@use img

                    } else {
                        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                        val pixelList = ArrayList<Color>()

                        stream.readChunked { pixels -> pixels.forEach { index -> pixelList.add(palette[index.toInt() and 0xFF]) } }

                        pixelList.coerceAtMost(width * height).forEachIndexed { index, color -> img.setRGB((index % width), (index / width), color.rgb) }

                        return@use img
                    }
                }
                "FS" -> {
                    val width = stream.readNumber(2, unsigned = true).toInt()
                    val height = stream.readNumber(2, unsigned = true).toInt()
                    val unknown = stream.readNumber(2, unsigned = true)

                    val palette = ArrayList<Color>()

                    for (i in 0 until 256) {
                        val r = stream.read() and 0xFF
                        val g = stream.read() and 0xFF
                        val b = stream.read() and 0xFF
                        val a = stream.read() and 0xFF
                        palette.add(Color(b, g, r, a))
                    }

                    if (palette.all { it.red == 0 && it.green == 0 && it.blue == 0 && it.alpha == 0 }) {
                        println("Blank palette in FS")

                        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                        for (y in 0 until height)
                            for (x in 0 until width)
                                img.setRGB(x, y, Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF).rgb)
                        return@use img

                    } else {
                        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                        val pixelList = ArrayList<Color>()

                        stream.readChunked { pixels -> pixels.forEach { index -> pixelList.add(palette[index.toInt() and 0xFF]) } }

                        pixelList.coerceAtMost(width * height).forEachIndexed { index, color -> img.setRGB((index % width), (index / width), color.rgb) }

                        return@use img
                    }
                }
                "Ff" -> {
                    val width = stream.readNumber(2, unsigned = true).toInt()
                    val height = stream.readNumber(2, unsigned = true).toInt()
                    val unknown = stream.readNumber(2, unsigned = true)

                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    for (y in 0 until height)
                        for (x in 0 until width)
                            img.setRGB(x, y, Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF).rgb)
                    return@use img
                }
                "FF" -> {
                    val width = stream.readNumber(2, unsigned = true).toInt()
                    val height = stream.readNumber(2, unsigned = true).toInt()
                    val unknown = stream.readNumber(2, unsigned = true)

                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    for (y in 0 until height)
                        for (x in 0 until width) {
                            val r = stream.read() and 0xFF
                            val g = stream.read() and 0xFF
                            val b = stream.read() and 0xFF
                            val a = stream.read() and 0xFF
                            img.setRGB(x, y, Color(b, g, r, a).rgb)
                        }
                    return@use img
                }
                else -> {
                }
            }

            throw IllegalArgumentException("${source.location} does not conform to the $name format (Reached end of function)")
        }
    }
}