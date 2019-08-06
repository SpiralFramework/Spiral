@file:Suppress("DuplicatedCode")

package net.npe.tga

import java.awt.image.BufferedImage

/*
  TGAWriter.java

  Copyright (c) 2015 Kenji Sasaki
  Released under the MIT license.
  https://github.com/npedotnet/TGAReader/blob/master/LICENSE

  English document
  https://github.com/npedotnet/TGAReader/blob/master/README.md

  Japanese document
  http://3dtech.jp/wiki/index.php?TGAReader

 */
object TGAWriter {

    enum class EncodeType {
        NONE, // No RLE encoding
        RLE, // RLE encoding
        AUTO
        // auto
    }

    fun write(pixels: IntArray, width: Int, height: Int, order: TGAReader.Order): ByteArray {
        return write(pixels, width, height, order, EncodeType.AUTO)
    }

    fun write(pixels: IntArray, width: Int, height: Int, order: TGAReader.Order, encodeType: EncodeType): ByteArray {

        val elementCount = if (hasAlpha(pixels, order)) 4 else 3

        val rawSize = elementCount * pixels.size
        val rleSize = getEncodeSize(pixels, width, elementCount)

        val encoding: Boolean
        val dataSize: Int

        when (encodeType) {
            TGAWriter.EncodeType.RLE -> {
                encoding = true
                dataSize = rleSize
            }
            TGAWriter.EncodeType.AUTO -> {
                encoding = rleSize < rawSize
                dataSize = if (encoding) rleSize else rawSize
            }
            else -> {
                // raw
                encoding = false
                dataSize = rawSize
            }
        }

        val length = 18 + FOOTER.size + dataSize

        val buffer = ByteArray(length)

        var index = 0

        // Header
        buffer[index++] = 0 // idFieldLength
        buffer[index++] = 0 // colormapType
        buffer[index++] = (if (encoding) 10 else 2).toByte() // RGB or RGB_RLE
        buffer[index++] = 0
        buffer[index++] = 0 // colormapOrigin
        buffer[index++] = 0
        buffer[index++] = 0 // colormapLength
        buffer[index++] = 0 // colormapDepth
        buffer[index++] = 0
        buffer[index++] = 0 // originX
        buffer[index++] = 0
        buffer[index++] = 0 // originY
        buffer[index++] = (width shr 0 and 0xFF).toByte() // width
        buffer[index++] = (width shr 8 and 0xFF).toByte() // width
        buffer[index++] = (height shr 0 and 0xFF).toByte() // height
        buffer[index++] = (height shr 8 and 0xFF).toByte() // height
        buffer[index++] = (8 * elementCount).toByte() // depth
        buffer[index++] = 0x20 // descriptor TODO alpha channel depth

        if (encoding) {
            index = encodeRLE(pixels, width, elementCount, order, buffer, index)
        } else {
            index = writeRaw(pixels, buffer, index, elementCount, order)
        }

        // Copy Footer
        for (i in FOOTER.indices) {
            buffer[index++] = FOOTER[i]
        }

        return buffer

    }

    private fun writeRaw(pixels: IntArray, buffer: ByteArray, index: Int, elementCount: Int, order: TGAReader.Order): Int {
        var index = index
        if (elementCount == 3) {
            // BGR
            for (i in pixels.indices) {
                buffer[index++] = (pixels[i] shr order.blueShift and 0xFF).toByte()
                buffer[index++] = (pixels[i] shr order.greenShift and 0xFF).toByte()
                buffer[index++] = (pixels[i] shr order.redShift and 0xFF).toByte()
            }
        } else {
            // BGRA
            for (i in pixels.indices) {
                buffer[index++] = (pixels[i] shr order.blueShift and 0xFF).toByte()
                buffer[index++] = (pixels[i] shr order.greenShift and 0xFF).toByte()
                buffer[index++] = (pixels[i] shr order.redShift and 0xFF).toByte()
                buffer[index++] = (pixels[i] shr order.alphaShift and 0xFF).toByte()
            }
        }
        return index
    }

    private val MODE_RESET = 0
    private val MODE_SELECT = 1
    private val MODE_SAME_COLOR = 2
    private val MODE_DIFFERENT_COLOR = 3

    private fun getEncodeSize(pixels: IntArray, width: Int, elementCount: Int): Int {

        var size = 0
        var color = 0
        var mode = MODE_RESET
        var start = 0

        for (i in pixels.indices) {
            if (mode == MODE_RESET) {
                color = pixels[i]
                mode = MODE_SELECT
                start = i
            } else if (mode == MODE_SELECT) {
                mode = if (color == pixels[i]) MODE_SAME_COLOR else MODE_DIFFERENT_COLOR
                color = pixels[i]
            } else if (mode == MODE_SAME_COLOR) {
                if (color != pixels[i]) {
                    // packet + rleData
                    size += 1 + elementCount
                    mode = MODE_SELECT
                    color = pixels[i]
                    start = i
                } else if (i - start >= 127) {
                    size += 1 + elementCount
                    mode = MODE_RESET
                }
            } else if (mode == MODE_DIFFERENT_COLOR) {
                if (color == pixels[i]) {
                    // packet + rawData * count
                    size += 1 + elementCount * (i - 1 - start)
                    mode = MODE_SAME_COLOR
                    color = pixels[i]
                    start = i - 1
                } else if (i - start >= 127) {
                    size += 1 + elementCount * 128
                    mode = MODE_RESET
                }
            }

            if ((i + 1) % width == 0 && mode != MODE_RESET) {
                if (mode == MODE_SAME_COLOR) {
                    size += 1 + elementCount
                } else {
                    // MODE_SELECT or MODE_DIFFERENT_COLOR
                    size += 1 + elementCount * (i - start + 1)
                }
                mode = MODE_RESET
            }

            // update color
            color = pixels[i]
        }

        if (mode != MODE_RESET) {
            println("Error!")
        }

        return size

    }

    private fun encodeRLE(pixels: IntArray, width: Int, elementCount: Int, order: TGAReader.Order, buffer: ByteArray, index: Int): Int {
        var index = index

        var color = 0
        var mode = MODE_RESET
        var start = 0

        for (i in pixels.indices) {
            if (mode == MODE_RESET) {
                color = pixels[i]
                mode = MODE_SELECT
                start = i
            } else if (mode == MODE_SELECT) {
                mode = if (color == pixels[i]) MODE_SAME_COLOR else MODE_DIFFERENT_COLOR
                color = pixels[i]
            } else if (mode == MODE_SAME_COLOR) {
                if (color != pixels[i]) {
                    // packet + rleData
                    index = encodeRLE(buffer, index, color, i - start, elementCount, order)
                    mode = MODE_SELECT
                    color = pixels[i]
                    start = i
                } else if (i - start >= 127) {
                    index = encodeRLE(buffer, index, color, 128, elementCount, order)
                    mode = MODE_RESET
                }
            } else if (mode == MODE_DIFFERENT_COLOR) {
                if (color == pixels[i]) {
                    // packet + rawData * count
                    index = encodeRLE(buffer, index, pixels, start, i - 1 - start, elementCount, order)
                    mode = MODE_SAME_COLOR
                    color = pixels[i]
                    start = i - 1
                } else if (i - start >= 127) {
                    index = encodeRLE(buffer, index, pixels, start, 128, elementCount, order)
                    mode = MODE_RESET
                }
            }

            if ((i + 1) % width == 0 && mode != MODE_RESET) {
                if (mode == MODE_SAME_COLOR) {
                    index = encodeRLE(buffer, index, color, i - start + 1, elementCount, order)
                } else {
                    // MODE_SELECT or MODE_DIFFERENT_COLOR
                    index = encodeRLE(buffer, index, pixels, start, i - start + 1, elementCount, order)
                }
                mode = MODE_RESET
            }

            // update color
            color = pixels[i]
        }

        if (mode != MODE_RESET) {
            println("Error!")
        }

        return index

    }

    private fun encodeRLE(buffer: ByteArray, index: Int, color: Int, count: Int, elementCount: Int, order: TGAReader.Order): Int {
        var index = index
        buffer[index++] = (0x80 or count - 1).toByte()
        buffer[index++] = (color shr order.blueShift and 0xFF).toByte()
        buffer[index++] = (color shr order.greenShift and 0xFF).toByte()
        buffer[index++] = (color shr order.redShift and 0xFF).toByte()
        if (elementCount == 4) {
            buffer[index++] = (color shr order.alphaShift and 0xFF).toByte()
        }
        return index
    }

    private fun encodeRLE(buffer: ByteArray, index: Int, pixels: IntArray, start: Int, count: Int, elementCount: Int, order: TGAReader.Order): Int {
        var index = index
        buffer[index++] = (count - 1).toByte()
        for (i in 0 until count) {
            val color = pixels[start + i]
            buffer[index++] = (color shr order.blueShift and 0xFF).toByte()
            buffer[index++] = (color shr order.greenShift and 0xFF).toByte()
            buffer[index++] = (color shr order.redShift and 0xFF).toByte()
            if (elementCount == 4) {
                buffer[index++] = (color shr order.alphaShift and 0xFF).toByte()
            }
        }
        return index
    }

    private fun hasAlpha(pixels: IntArray, order: TGAReader.Order): Boolean {
        val alphaShift = order.alphaShift
        for (i in pixels.indices) {
            val alpha = pixels[i] shr alphaShift and 0xFF
            if (alpha != 0xFF) return true
        }
        return false
    }

    private val FOOTER = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 84, 82, 85, 69, 86, 73, 83, 73, 79, 78, 45, 88, 70, 73, 76, 69, 46, 0) // TRUEVISION-XFILE

    fun writeImage(img: BufferedImage): ByteArray {
        return write(img.getRGB(0, 0, img.width, img.height, null, 0, img.width), img.width, img.height, TGAReader.ARGB)
    }
}