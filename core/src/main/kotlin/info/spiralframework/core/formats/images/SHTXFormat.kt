package info.spiralframework.core.formats.images

import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.*
import java.awt.image.BufferedImage
import java.io.OutputStream

object SHTXFormat: ReadableSpiralFormat<BufferedImage>, WritableSpiralFormat {
    //TODO: Separate the different "sub formats" into their own objects
    override val name: String = "shtx"

    val SHTX_MAGIC_NUMBER = 0x58544853
    /** SHTXFs */
    val RGBA_PALETTE_MAGIC_NUMBER = 0x7346
    /** SHTXFS */
    val BGRA_PALETTE_MAGIC_NUMBER = 0x5346
    /** SHTXFf */
    val RGBA_MAGIC_NUMBER = 0x6646
    /** SHTXFF */
    val BGRA_MAGIC_NUMBER = 0x4646

    /**
     * Attempts to read the data source as [T]
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param source A function that returns an input stream
     *
     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
     */
    override fun read(name: String?, game: DRGame?, context: DataContext, source: DataSource): FormatResult<BufferedImage> {
        source().use { stream ->
            val magic = stream.readInt32LE()

            if (magic != SHTX_MAGIC_NUMBER)
                return FormatResult.Fail(1.0)

            val formatMagic = stream.readInt16LE()
            val width = stream.readInt16LE()
            val height = stream.readInt16LE()
            val unk = stream.readInt16LE()

            when (formatMagic) {
                RGBA_PALETTE_MAGIC_NUMBER -> {
                    val palette = IntArray(256) { RGBA(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF) }

                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    if (palette.all { colour -> colour == 0 }) {
//                        println("Blank palette in Fs")
                        val rgb = IntArray(width * height)
                        val row = ByteArray(4 * width)
                        var maxX: Int

                        for (y in 0 until height) {
                            maxX = stream.read(row)

                            for (x in 0 until maxX step 4) {
                                rgb[(y * width) + x] = RGBA(row[x], row[x + 1], row[x + 2], row[x + 3])
                            }
                        }

                        img.setRGB(0, 0, width, height, rgb, 0, width)
                    } else {
                        val rgb = IntArray(width * height)
                        val row = ByteArray(width)
                        var maxX: Int

                        for (y in 0 until height) {
                            maxX = stream.read(row)

                            for (x in 0 until maxX) {
                                rgb[(y * width) + x] = palette[row[x].toInt() and 0xFF]
                            }
                        }
                        img.setRGB(0, 0, width, height, rgb, 0, width)
                    }

                    return FormatResult.Success(img, 1.0)
                }
                BGRA_PALETTE_MAGIC_NUMBER -> {
                    val palette = IntArray(256) { BGRA(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF) }

                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    if (palette.all { colour -> colour == 0 }) {
//                        println("Blank palette in Fs")
                        val rgb = IntArray(width * height)
                        val row = ByteArray(4 * width)
                        var maxX: Int

                        for (y in 0 until height) {
                            maxX = stream.read(row)

                            for (x in 0 until maxX step 4) {
                                rgb[(y * width) + x] = BGRA(row[x], row[x + 1], row[x + 2], row[x + 3])
                            }
                        }

                        img.setRGB(0, 0, width, height, rgb, 0, width)
                    } else {
                        val rgb = IntArray(width * height)
                        val row = ByteArray(width)
                        var maxX: Int

                        for (y in 0 until height) {
                            maxX = stream.read(row)

                            for (x in 0 until maxX) {
                                rgb[(y * width) + x] = palette[row[x].toInt() and 0xFF]
                            }
                        }
                        img.setRGB(0, 0, width, height, rgb, 0, width)
                    }

                    return FormatResult.Success(img, 1.0)
                }
                RGBA_MAGIC_NUMBER -> {
                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    val rgb = IntArray(width * height)
                    val row = ByteArray(4 * width)
                    var maxX: Int

                    for (y in 0 until height) {
                        maxX = stream.read(row)

                        for (x in 0 until maxX step 4) {
                            rgb[(y * width) + x] = RGBA(row[x], row[x + 1], row[x + 2], row[x + 3])
                        }
                    }
                    img.setRGB(0, 0, width, height, rgb, 0, width)

                    return FormatResult.Success(img, 1.0)
                }
                BGRA_MAGIC_NUMBER -> {
                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    val rgb = IntArray(width * height)
                    val row = ByteArray(4 * width)
                    var maxX: Int

                    for (y in 0 until height) {
                        maxX = stream.read(row)

                        for (x in 0 until maxX step 4) {
                            rgb[(y * width) + x] = BGRA(row[x], row[x + 1], row[x + 2], row[x + 3])
                        }
                    }
                    img.setRGB(0, 0, width, height, rgb, 0, width)

                    return FormatResult.Success(img, 1.0)
                }
                else -> return FormatResult.Fail(1.0)
            }
        }
    }

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): EnumFormatWriteResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}