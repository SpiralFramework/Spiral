package org.abimon.spiral.core.formats

import org.abimon.spiral.core.*
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import org.abimon.visi.io.skipBytes
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.util.ArrayList
import javax.imageio.ImageIO
import kotlin.system.measureNanoTime

object DDS1DDSFormat : SpiralFormat {
    override val name: String = "DDS1DDS Texture"
    override val extension: String = ".dds"

    val BIT5 = intArrayOf(0, 8, 16, 25, 33, 41, 49, 58, 66, 74, 82, 90, 99, 107, 115, 123, 132, 140, 148, 156, 165, 173, 181, 189, 197, 206, 214, 222, 230, 239, 247, 255 )
    val BIT6 = intArrayOf(0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 97, 101, 105, 109, 113, 117, 121, 125, 130, 134, 138, 142, 146, 150, 154, 158, 162, 166, 170, 174, 178, 182, 186, 190, 194, 198, 202, 206, 210, 215, 219, 223, 227, 231, 235, 239, 243, 247, 251, 255)

    override fun isFormat(source: DataSource): Boolean = source.use { it.readString(8) == "DDS1DDS " }

    override fun canConvert(format: SpiralFormat): Boolean = format is PNGFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.use { stream ->
            val header = stream.read(132)
            val his = ByteArrayInputStream(header)

            val magic = his.readString(8)

            if (magic != "DDS1DDS ")
                throw IllegalArgumentException("\"$magic\" ≠ DDS1DDS ")

            val size = his.readUnsignedLittleInt()
            val flags = his.readUnsignedLittleInt()
            val height = his.readUnsignedLittleInt().toInt()
            val width = his.readUnsignedLittleInt().toInt()

            //Check the type here or something

            his.skipBytes(104)
            val caps2 = his.readUnsignedLittleInt()

            val texels = ArrayList<Array<Color>>()

            val time = measureNanoTime {
                var ms = System.nanoTime()
                (0 until ((height * width) / 16)).forEach { supposedIndex ->
                    val now = System.nanoTime()
                    debug("Last loop (It is now $supposedIndex) took ${now - ms} nanoseconds")
                    ms = now

                    val palette = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.BLACK)
                    val colourBytes = arrayOf(stream.readNumber(2, unsigned = true, little = true), stream.readNumber(2, unsigned = true, little = true)).map { it.toInt() }

                    (0 until 2).forEach {
                        val rgb565 = colourBytes[it]
//                    val r = ((rgb565 and 0xFC00) shr 11) shl 3
//                    val g = ((rgb565 and 0x07E0) shr 5) shl 2
//                    val b = (rgb565 and 0x001F) shl 3
//
                        val r = BIT5[((rgb565 and 0xFC00) shr 11)]
                        val g = BIT6[((rgb565 and 0x07E0) shr 5)]
                        val b = BIT5[(rgb565 and 0x001F)]

                        palette[it] = Color(r, g, b)
                    }

                    if (colourBytes[0] > colourBytes[1]) {
                        palette[2] = Color(
                                (2 * BIT5[colourBytes[0] and 0xFC00 shr 11] + BIT5[colourBytes[1] and 0xFC00 shr 11]) / 3,
                                (2 * BIT6[colourBytes[0] and 0x07E0 shr 5] + BIT6[colourBytes[1] and 0x07E0 shr 5]) / 3,
                                (2 * BIT5[colourBytes[0] and 0x001F] + BIT5[colourBytes[1] and 0x001F]) / 3
                        )

                        palette[3] = Color(
                                (2 * BIT5[colourBytes[1] and 0xFC00 shr 11] + BIT5[colourBytes[0] and 0xFC00 shr 11]) / 3,
                                (2 * BIT6[colourBytes[1] and 0x07E0 shr 5] + BIT6[colourBytes[0] and 0x07E0 shr 5]) / 3,
                                (2 * BIT5[colourBytes[1] and 0x001F] + BIT5[colourBytes[0] and 0x001F]) / 3
                        )
                    } else {
                        palette[2] = Color((palette[0].red + palette[1].red) / 2, (palette[0].green + palette[1].green) / 2, (palette[0].blue + palette[1].blue) / 2)
                        palette[3] = Color(0, 0, 0, 0)
                    }

                    val texel = ArrayList<Color>()

                    (0 until 4).forEach {
                        val byte = stream.read()
                        //OH LET'S BREAK IT ***DOWN***

                        val bitsOne = "${byte.getBit(7)}${byte.getBit(6)}".toInt(2)
                        val bitsTwo = "${byte.getBit(5)}${byte.getBit(4)}".toInt(2)
                        val bitsThree = "${byte.getBit(3)}${byte.getBit(2)}".toInt(2)
                        val bitsFour = "${byte.getBit(1)}${byte.getBit(0)}".toInt(2)

                        texel.addAll(arrayOf(palette[bitsFour], palette[bitsThree], palette[bitsTwo], palette[bitsOne]))
                    }

                    texels.add(texel.toTypedArray())
                }
            }

            if(isDebug) println("Took $time ns to read")

            val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

            val rows = ArrayList<Array<Color>>()
            val tmpRows = arrayOf(ArrayList<Color>(), ArrayList<Color>(), ArrayList<Color>(), ArrayList<Color>())

            texels.forEachIndexed { index, texel ->
                if (index % (width / 4) == 0 && tmpRows.any { it.isNotEmpty() }) {
                    tmpRows.forEach { rows.add(it.toTypedArray()); it.clear() }
                }

                texel.forEachIndexed { i, color -> tmpRows[i / 4].add(color) }
            }

            tmpRows.forEach { rows.add(it.toTypedArray()); it.clear() }

            if(isDebug) println("Setting pixels through an iterator took ${measureNanoTime { rows.forEachIndexed { y, row -> row.forEachIndexed { x, color -> img.setRGB(x, y, color.rgb) } } }} ns")
            else rows.forEachIndexed { y, row -> row.forEachIndexed { x, color -> img.setRGB(x, y, color.rgb) } }

            ImageIO.write(img, "PNG", output)
        }
    }
}