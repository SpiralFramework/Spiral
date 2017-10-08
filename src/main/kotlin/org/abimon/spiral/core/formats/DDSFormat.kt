package org.abimon.spiral.core.formats

import org.abimon.karnage.raw.DXT1PixelData
import org.abimon.spiral.core.*
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import org.abimon.visi.io.skipBytes
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.util.*
import javax.imageio.ImageIO

object DDSFormat : SpiralFormat {
    override val name: String = "DDS Texture"
    override val extension: String = ".dds"
    override val conversions: Array<SpiralFormat> = arrayOf(PNGFormat, TGAFormat, SHTXFormat, JPEGFormat)

    val BIT5 = intArrayOf(0, 8, 16, 25, 33, 41, 49, 58, 66, 74, 82, 90, 99, 107, 115, 123, 132, 140, 148, 156, 165, 173, 181, 189, 197, 206, 214, 222, 230, 239, 247, 255 )
    val BIT6 = intArrayOf(0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 97, 101, 105, 109, 113, 117, 121, 125, 130, 134, 138, 142, 146, 150, 154, 158, 162, 166, 170, 174, 178, 182, 186, 190, 194, 198, 202, 206, 210, 215, 219, 223, 227, 231, 235, 239, 243, 247, 251, 255)

    override fun isFormat(source: DataSource): Boolean = source.use { it.readString(8) == "DDS1DDS " }

    override fun canConvert(format: SpiralFormat): Boolean = format is PNGFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        val img = source.use { stream ->
            val header = stream.read(132)
            val his = ByteArrayInputStream(header)

            val magic = his.readString(8)

            if (magic != "DDS1DDS ")
                throw IllegalArgumentException("\"$magic\" ≠ DDS1DDS ")

            his.readUnsignedLittleInt() //Size
            his.readUnsignedLittleInt() //Flags
            val height = his.readUnsignedLittleInt().toInt()
            val width = his.readUnsignedLittleInt().toInt()

            //Check the type here or something

            his.skipBytes(104)
            his.readUnsignedLittleInt() //caps2

            return@use DXT1PixelData.read(width, height, stream)
        }

        when(format) {
            is PNGFormat -> ImageIO.write(img, "PNG", output)
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPEG", output)
            is TGAFormat -> output.write(img.toTGA())
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

        return true
    }
}