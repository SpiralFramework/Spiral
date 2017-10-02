package org.abimon.spiral.core.data

import org.abimon.spiral.core.formats.DDS1DDSFormat
import org.abimon.spiral.core.readNumber
import java.awt.Color
import java.awt.image.BufferedImage

enum class RawImageData(private val func: (Int, Int, ByteArray) -> BufferedImage) {
    DXT1(func@ { width, height, data ->
        return@func data.inputStream().use baUse@ { bais ->
            val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            for (supposedIndex in 0 until ((height * width) / 16)) {
                val texel_palette = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.BLACK)
                val colourBytes = arrayOf(bais.readNumber(2, unsigned = true, little = false), bais.readNumber(2, unsigned = true, little = false)).map { it.toInt() }

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
                    val byte = bais.read()
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
            return@baUse img
        }
    }),

    BC7(func@ { width, height, data ->
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val bits = BitPool(data)
        loop@for (supposedIndex in 0 until ((height * width) / 16)) {
            var modeBit: Int = 0
            for(i in 0 until 8) {
                if(bits[1] == 1)
                    break
                modeBit++
            }

            val mode: Array<Any>

            when(modeBit) {
                0 -> {
                    val partition = bits[4]
                    val red = (0 until 6).map { bits[4] }
                    val green = (0 until 6).map { bits[4] }
                    val blue = (0 until 6).map { bits[4] }
                    val p = (0 until 6).map { bits[1] }
                    val index = bits[45]

                    mode = arrayOf(partition, red, green, blue, p, index)
                }
                1 -> {
                    val partition = bits[6]
                    val red = (0 until 4).map { bits[6] }
                    val green = (0 until 4).map { bits[6] }
                    val blue = (0 until 4).map { bits[6] }
                    val p = (0 until 2).map { bits[1] }
                    val index = bits[46]

                    mode = arrayOf(partition, red, green, blue, p, index)
                }
                else -> { bits[127-modeBit]; continue@loop }
            }
        }

        return@func img
    })
    ;

    operator fun invoke(width: Int, height: Int, data: ByteArray): BufferedImage = func(width, height, data)
}