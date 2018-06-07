package org.abimon.spiral.core.objects.images

import org.abimon.karnage.raw.BC4PixelData
import org.abimon.karnage.raw.BC7PixelData
import org.abimon.karnage.raw.DXT1PixelData
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.images.SRDFormat.deswizzle
import org.abimon.spiral.core.hasBitSet
import org.abimon.spiral.core.utils.WindowedInputStream
import org.abimon.visi.io.DataSource
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream

open class SRDItem(val dataType: String, val dataOffset: Long, val dataLen: Long, val subdataOffset: Long, val subdataLen: Long, val parent: DataSource) {
    val data: WindowedInputStream
        get() = WindowedInputStream(parent.seekableInputStream, dataOffset, dataLen)
    val subdata: WindowedInputStream
        get() = WindowedInputStream(parent.seekableInputStream, subdataOffset, subdataLen)

    operator fun component1(): String = dataType
    operator fun component2(): WindowedInputStream = data
    operator fun component3(): WindowedInputStream = subdata
}

class TXRItem(
        val unk1: Long, val swiz: Int, val dispWidth: Int, val dispHeight: Int,
        val scanline: Int, val format: Int, val unk2: Int, val palette: Int,
        val paletteId: Int, val unk5: Int, val mipmaps: Array<IntArray>,
        val name: String, val parentItem: SRDItem, val imageItem: SRDItem
): SRDItem("\$TXR", parentItem.dataOffset, parentItem.dataLen, parentItem.subdataOffset, parentItem.subdataLen, parentItem.parent) {
    fun readTexture(srdv: InputStream): BufferedImage? {
        val texture = WindowedInputStream(srdv, mipmaps[0][0].toLong(), mipmaps[0][1].toLong())

        val swizzled = !(swiz hasBitSet 1)
        if (format in arrayOf(0x01, 0x02, 0x05, 0x1A)) {
            val bytespp: Int

            when (format) {
                0x01 -> bytespp = 4
                0x02 -> bytespp = 2
                0x05 -> bytespp = 2
                0x1A -> bytespp = 4
                else -> bytespp = 2
            }

            val width: Int = dispWidth.toInt() //(scanline / bytespp).toInt()
            val height: Int = dispHeight.toInt()

            val processing: InputStream

            if (swizzled) {
                val processingData = texture.use { it.readBytes() }
                processingData.deswizzle(width / 4, height / 4, bytespp)
                processing = processingData.inputStream()
            } else
                processing = texture

            when (format) {
                0x01 -> {
                    val resultingImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            val b = processing.read()
                            val g = processing.read()
                            val r = processing.read()
                            val a = processing.read()

                            resultingImage.setRGB(x, y, Color(r, g, b, a).rgb)
                        }
                    }

                    return resultingImage
                }
                else -> {
                    SpiralData.LOGGER.debug("Raw format: {} (0x{})", format, format.toString(16))
                    return null
                }
            }
        } else if (format in arrayOf(0x0F, 0x11, 0x14, 0x16, 0x1C)) {
            val bytespp: Int

            when (format) {
                0x0F -> bytespp = 8
                0x1C -> bytespp = 16
                else -> bytespp = 8
            }

            var width: Int = dispWidth
            var height: Int = dispHeight

            if (width % 4 != 0)
                width += 4 - (width % 4)

            if (height % 4 != 0)
                height += 4 - (height % 4)

            val processingStream: InputStream

            if (swizzled && width >= 4 && height >= 4) {
                val processingData = texture.use { it.readBytes() }
                processingData.deswizzle(width / 4, height / 4, bytespp)
                processingStream = processingData.inputStream()
            } else
                processingStream = texture

            when (format) {
                0x0F -> return DXT1PixelData.read(width, height, processingStream)
                0x16 -> return BC4PixelData.read(width, height, processingStream)
                0x1C -> return BC7PixelData.read(width, height, processingStream)
                else -> {
                    SpiralData.LOGGER.debug("Block format: {} (0x{}) [{}x{}]", format, format.toString(16), width, height)
                    return null
                }
            }
        } else
            SpiralData.LOGGER.debug("Other format: {} (0x{})", format, format.toString(16))

        return null
    }
}