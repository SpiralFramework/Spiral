package org.abimon.spiral.core.formats

import org.abimon.spiral.core.*
import org.abimon.spiral.util.debug
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import org.abimon.visi.io.skipBytes
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.system.measureNanoTime

object DDS1DDSFormat : SpiralFormat {
    override val name: String = "DDS1DDS Texture"
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

            val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val time = measureNanoTime {
                for (supposedIndex in 0 until ((height * width) / 16)) {
                    val texel_palette = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.BLACK)
                    val colourBytes = arrayOf(stream.readNumber(2, unsigned = true, little = false), stream.readNumber(2, unsigned = true, little = false)).map { it.toInt() }

                    (0 until 2).forEach {
                        val rgb565 = colourBytes[it]

                        val r = DDS1DDSFormat.BIT5[((rgb565 and 0xFC00) shr 11)]
                        val g = DDS1DDSFormat.BIT6[((rgb565 and 0x07E0) shr 5)]
                        val b = DDS1DDSFormat.BIT5[(rgb565 and 0x001F)]

                        texel_palette[it] = Color(r, g, b)
                    }

                    if (colourBytes[0] > colourBytes[1]) {
                        texel_palette[2] = Color(
                                (2 * DDS1DDSFormat.BIT5[colourBytes[0] and 0xFC00 shr 11] + DDS1DDSFormat.BIT5[colourBytes[1] and 0xFC00 shr 11]) / 3,
                                (2 * DDS1DDSFormat.BIT6[colourBytes[0] and 0x07E0 shr 5] + DDS1DDSFormat.BIT6[colourBytes[1] and 0x07E0 shr 5]) / 3,
                                (2 * DDS1DDSFormat.BIT5[colourBytes[0] and 0x001F] + DDS1DDSFormat.BIT5[colourBytes[1] and 0x001F]) / 3
                        )

                        texel_palette[3] = Color(
                                (2 * DDS1DDSFormat.BIT5[colourBytes[1] and 0xFC00 shr 11] + DDS1DDSFormat.BIT5[colourBytes[0] and 0xFC00 shr 11]) / 3,
                                (2 * DDS1DDSFormat.BIT6[colourBytes[1] and 0x07E0 shr 5] + DDS1DDSFormat.BIT6[colourBytes[0] and 0x07E0 shr 5]) / 3,
                                (2 * DDS1DDSFormat.BIT5[colourBytes[1] and 0x001F] + DDS1DDSFormat.BIT5[colourBytes[0] and 0x001F]) / 3
                        )
                    } else {
                        texel_palette[2] = Color((texel_palette[0].red + texel_palette[1].red) / 2, (texel_palette[0].green + texel_palette[1].green) / 2, (texel_palette[0].blue + texel_palette[1].blue) / 2)
                        texel_palette[3] = Color(0, 0, 0, 0)
                    }

                    (0 until 4).forEach { i ->
                        val byte = stream.read()
                        //OH LET'S BREAK IT ***DOWN***

                        val bitsOne = byte shr 6 and 0b11   //"${byte.getBit(7)}${byte.getBit(6)}".toInt(2)
                        val bitsTwo = byte shr 4 and 0b11   //"${byte.getBit(5)}${byte.getBit(4)}".toInt(2)
                        val bitsThree = byte shr 2 and 0b11 //"${byte.getBit(3)}${byte.getBit(2)}".toInt(2)
                        val bitsFour = byte shr 0 and 0b11  //"${byte.getBit(1)}${byte.getBit(0)}".toInt(2)

                        img.setRGB((supposedIndex % (width / 4)) * 4, (supposedIndex / (width / 4)) * 4 + i, texel_palette[bitsFour].rgb)
                        img.setRGB((supposedIndex % (width / 4)) * 4 + 1, (supposedIndex / (width / 4)) * 4 + i, texel_palette[bitsThree].rgb)
                        img.setRGB((supposedIndex % (width / 4)) * 4 + 2, (supposedIndex / (width / 4)) * 4 + i, texel_palette[bitsTwo].rgb)
                        img.setRGB((supposedIndex % (width / 4)) * 4 + 3, (supposedIndex / (width / 4)) * 4 + i, texel_palette[bitsOne].rgb)
                    }
                }
            }

            debug("Took $time ns to read")

            return@use img
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

                    palette.forEach { colour -> output.write(org.abimon.visi.collections.byteArrayOf(colour.red, colour.green, colour.blue, colour.alpha)) }

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