package org.abimon.spiral.core.formats.images

import org.abimon.karnage.raw.BC7PixelData
import org.abimon.karnage.raw.DXT1PixelData
import org.abimon.spiral.core.*
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.archives.SPCFormat
import org.abimon.spiral.core.formats.archives.ZIPFormat
import org.abimon.spiral.core.objects.archives.SPC
import org.abimon.spiral.core.objects.models.SRDIModel
import org.abimon.spiral.util.*
import org.abimon.visi.collections.remove
import org.abimon.visi.io.ByteArrayDataSource
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.and
import org.abimon.visi.lang.exportStackTrace
import java.awt.*
import java.awt.geom.Area
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO
import kotlin.math.log
import kotlin.math.min

object SRDFormat {
    fun hook() {
        SpiralFormat[SPCFormat to PNGFormat] = this::convertFromArchive
        SpiralFormat[SPCFormat to ZIPFormat] = this::convertFromArchive
    }

    fun convertFromArchive(from: SpiralFormat, to: SpiralFormat, dataSource: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (!"${params["srd:convert"] ?: true}".toBoolean())
            return false

        val otherEntries: MutableMap<String, DataSource> = HashMap()
        val images: MutableMap<String, BufferedImage> = HashMap()
        val mapToModels: Boolean = "${params["srd:mapToModels"] ?: true}".toBoolean()
        var imageOverride: Boolean = false

        when (from) {
            is SPCFormat -> {
                val spc = SPC(dataSource)

                val files = spc.files.groupBy { entry -> entry.name.endsWith("srd") }
                val srds = files[true] ?: emptyList()
                val others = files[false]!!.toMutableList()

                srds.forEach { srdEntry ->
                    val img: DataSource

                    if (others.any { entry -> entry.name == srdEntry.name.replaceAfterLast('.', "srdv") })
                        img = others.remove { entry -> entry.name == srdEntry.name.replaceAfterLast('.', "srdv") } ?: return@forEach
                    else
                        img = others.firstOrNull { entry -> entry.name == srdEntry.name.replaceAfterLast('.', "srdi") } ?: run { debug("No such element for ${srdEntry.name.substringBeforeLast('.')}"); otherEntries[srdEntry.name] = srdEntry; return@forEach }

                    val model: DataSource? = if (mapToModels) others.firstOrNull { entry -> entry.name == srdEntry.name.split('.')[0] + ".srdi" } else null

                    val before = otherEntries.size
                    readSRD(srdEntry, img, if (model != null) SRDIModel(model) else null, otherEntries, images, params)
                    if (otherEntries.size != before)
                        imageOverride = true
                }

                others.forEach { entry -> otherEntries[entry.name] = entry }
            }
            else -> throw IllegalArgumentException("Unknown archive to convert from!")
        }

        if (!imageOverride && images.isEmpty() && !LoggerLevel.TRACE.enabled)
            return false

        val format = SpiralFormats.formatForName(params["srd:format"]?.toString() ?: "PNG", SpiralFormats.imageFormats)
                ?: PNGFormat
        when (to) {
            is PNGFormat -> ImageIO.write(images.entries.first().value, "PNG", output)
            is ZIPFormat -> {
                val zos = ZipOutputStream(output)

                otherEntries.forEach { name, data ->
                    zos.putNextEntry(ZipEntry(name))
                    data.pipe(zos)
                }

                images.forEach { name, image ->
                    zos.putNextEntry(ZipEntry(name.replaceAfterLast('.', format.extension!!)))
                    if (format is PNGFormat)
                        ImageIO.write(image, "PNG", zos)
                    else
                        PNGFormat.convert(format, image, zos, emptyMap())
                }

                zos.finish()
            }
            else -> return false
        }

        return true
    }

    fun readSRD(srd: DataSource, img: DataSource, model: SRDIModel?, otherData: MutableMap<String, DataSource>, images: MutableMap<String, BufferedImage>, params: Map<String, Any?>) {
        srd.seekableUse { original ->
            val stream = CountingInputStream(original)
            loop@ while (true) {
                val (data_type, data_S, subdata_S) = readSRDItem(stream, srd) ?: break

                subdata_S.use big@{ subdata ->
                    data_S.use { data ->

                        //debug(data_type)

                        when (data_type) {
                            "\$CFH" -> return@big
                            "\$CT0" -> return@big
                            "\$RSF" -> {
                                data.skip(16)
                                val name = data.readZeroString()
                                //println(name)
                            }
                            "\$TXR" -> {
                                val unk1 = data.readUnsignedLittleInt()
                                val swiz = data.readNumber(2, unsigned = true, little = true)
                                var disp_width = data.readNumber(2, unsigned = true, little = true)
                                var disp_height = data.readNumber(2, unsigned = true, little = true)
                                var scanline = data.readNumber(2, unsigned = true, little = true)
                                val fmt = data.read() and 0xFF
                                val unk2 = data.read() and 0xFF
                                val palette = data.read() and 0xFF
                                val palette_id = data.read() and 0xFF

                                val (img_data_type, img_data_S, img_subdata_S) = readSRDItem(subdata, srd)!!
                                img_subdata_S.close()
                                img_data_S.use { img_data ->
                                    img_data.skip(2)
                                    val unk5 = img_data.read() and 0xFF
                                    val mipmap_count = img_data.read() and 0xFF
                                    img_data.skip(8)
                                    val name_offset = img_data.readUnsignedLittleInt()

                                    val mipmaps: MutableList<IntArray> = ArrayList()

                                    for (i in 0 until mipmap_count) {
                                        val mipmap_start = img_data.readUnsignedLittleInt() and 0x00FFFFFF
                                        val mipmap_len = img_data.readUnsignedLittleInt()
                                        val mipmap_unk1 = img_data.readUnsignedLittleInt()
                                        val mipmap_unk2 = img_data.readUnsignedLittleInt()

                                        mipmaps.add(intArrayOf(mipmap_start.toInt(), mipmap_len.toInt(), mipmap_unk1.toInt(), mipmap_unk2.toInt()))
                                    }

                                    val pal_start: Int?
                                    val pal_len: Int?

                                    if (palette == 0x01) {
                                        pal_start = mipmaps[palette_id][0]
                                        pal_len = mipmaps[palette_id][1]

                                        mipmaps.removeAt(palette_id)
                                    } else {
                                        pal_start = null
                                        pal_len = null
                                    }

                                    img_data.reset()
                                    img_data.skip(name_offset)

                                    val name = img_data.readZeroString()
                                    val doMips = "${params["srd:extractMipmaps"] ?: false}".toBoolean()
                                    if (!doMips)
                                        (1 until mipmaps.size).forEach { mipmaps.removeAt(1) }

                                    for (mip in mipmaps.indices) {
                                        val mipName = if (doMips) "$mip-$name" else name
                                        val (mipmap_start, mipmap_len, mipmap_unk1, mipmap_unk2) = mipmaps[mip]
                                        val new_img_stream = OffsetInputStream(img.seekableInputStream, mipmap_start.toLong(), mipmap_len.toLong())
//                                    var pal_data: ByteArray? = null
//
//                                    img.seekableInputStream.use { img_stream ->
//                                        if (pal_start != null) {
//                                            img_stream.reset()
//                                            img_stream.skip(pal_start.toLong())
//
//                                            pal_data = ByteArray(pal_len!!).apply { img_stream.read(this) }
//                                        }
//                                    }

                                        val swizzled = !(swiz hasBitSet 1)
                                        val imageInfo: MutableMap<String, Any> = hashMapOf("format" to fmt, "disp_width" to disp_width, "disp_height" to disp_height, "name" to name, "mipmap" to mip)

                                        val mode = "RGBA"
                                        if (fmt in arrayOf(0x01, 0x02, 0x05, 0x1A)) {
                                            debug("Raw $fmt")
                                            val decoder = "raw"

                                            val bytespp: Int

                                            when (fmt) {
                                                0x01 -> bytespp = 4
                                                0x02 -> bytespp = 2
                                                0x05 -> bytespp = 2
                                                0x1A -> bytespp = 4
                                                else -> bytespp = 2
                                            }

                                            val width: Int = disp_width.toInt() //(scanline / bytespp).toInt()
                                            val height: Int = disp_height.toInt()

                                            val processing: InputStream

                                            if (swizzled) {
                                                val processingData = new_img_stream.use { it.readBytes() }
                                                processing = processingData.deswizzle(width / 4, height / 4, bytespp).inputStream()
                                            } else
                                                processing = new_img_stream

                                            when (fmt) {
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
                                                    images[mipName] = resultingImage.mapToModel(model, params)
                                                }
                                                else -> otherData["$mip-$name ($fmt,$width,$height).dat"] = ByteArrayDataSource(processing.use { it.readBytes() })
                                            }

                                            imageInfo["swizzled"] = swizzled
                                            imageInfo["bytes_per_pixel"] = bytespp

                                        } else if (fmt in arrayOf(0x0F, 0x11, 0x14, 0x16, 0x1C)) {
                                            val bytespp: Int

                                            when (fmt) {
                                                0x0F -> bytespp = 8
                                                0x1C -> bytespp = 16
                                                else -> bytespp = 8
                                            }

                                            var width: Int
                                            var height: Int

                                            if (unk5 == 0x08) {
                                                width = lowestPowerOfTwo(disp_width.toInt())
                                                height = lowestPowerOfTwo(disp_height.toInt())
                                            } else {
                                                width = disp_width.toInt()
                                                height = disp_height.toInt()
                                            }

                                            if (width % 4 != 0)
                                                width += 4 - (width % 4)

                                            if (height % 4 != 0)
                                                height += 4 - (height % 4)

                                            val processingStream: InputStream

                                            trace("Does $mipName need deswizzling? ${swizzled && width >= 4 && height >= 4}")

                                            if (swizzled && width >= 4 && height >= 4) {
                                                val processingData = new_img_stream.use { it.readBytes() }
                                                processingStream = processingData.deswizzle(width / 4, height / 4, bytespp).inputStream()
                                            } else
                                                processingStream = new_img_stream

                                            when (fmt) {
                                                0x0F -> images[mipName] = processingStream.use { processing -> DXT1PixelData.read(width, height, processing) }.mapToModel(model, params)
                                                0x1C -> images[mipName] = processingStream.use { processing -> BC7PixelData.read(width, height, processing) }.mapToModel(model, params)
                                                else -> {
                                                    debug("Block Compression $fmt"); otherData["$mip-$name ($fmt,$width,$height).dat"] = ByteArrayDataSource(processingStream.use { it.readBytes() })
                                                }
                                            }

                                            imageInfo["swizzled"] = swizzled && width >= 4 && height >= 4
                                            imageInfo["bytes_per_pixel"] = bytespp
                                            imageInfo["width"] = width
                                            imageInfo["height"] = height
                                        } else {
                                            debug("Unknown format $fmt")

                                            otherData["$mip-$name ($fmt,$disp_width,$disp_height).dat"] = ByteArrayDataSource(new_img_stream.use { it.readBytes() })
                                        }

                                        if (LoggerLevel.TRACE.enabled)
                                            otherData["info-$mipName.json"] = ByteArrayDataSource(SpiralData.MAPPER.writeValueAsBytes(imageInfo))

                                        disp_width = maxOf(1, disp_width / 2)
                                        disp_height = maxOf(1, disp_height / 2)
                                        scanline = maxOf(1, scanline / 2)
                                    }
                                }
                            }
                            else -> debug("Unknown data type: $data_type")
                        }
                    }
                }
            }
        }
    }

    fun readSRDItem(stream: CountingInputStream, source: DataSource): Triple<String, OffsetInputStream, OffsetInputStream>? {
        val data_type = stream.readString(4)

        if (data_type.length < 4 || !data_type.startsWith("$"))
            return null

        val data_len = stream.readUnsignedBigInt()
        val subdata_len = stream.readUnsignedBigInt()
        val padding = stream.readUnsignedBigInt()

        val data_padding = (0x10 - data_len % 0x10) % 0x10
        val subdata_padding = (0x10 - subdata_len % 0x10) % 0x10

        val data = OffsetInputStream(source.seekableInputStream, if (stream is OffsetInputStream) stream.offset + stream.count else stream.count, data_len)  //ByteArray(data_len.toInt()).apply { stream.read(this) }
        stream.skip(data_padding + data_len)

        val subdata = OffsetInputStream(source.seekableInputStream, if (stream is OffsetInputStream) stream.offset + stream.count else stream.count, subdata_len) //ByteArray(subdata_len.toInt()).apply { stream.read(this) }
        stream.skip(subdata_padding + subdata_len)

        return data_type to data and subdata
    }

    fun lowestPowerOfTwo(num: Int): Int {
        var n = num - 1
        n = n or (n shr 1)
        n = n or (n shr 2)
        n = n or (n shr 4)
        n = n or (n shr 8)
        n = n or (n shr 16)
        return n + 1
    }

    fun compact1By1(num: Int): Int {
        var x = num
        x = x and 0x55555555
        x = (x xor (x shr 1)) and 0x33333333
        x = (x xor (x shr 2)) and 0x0f0f0f0f
        x = (x xor (x shr 4)) and 0x00ff00ff
        x = (x xor (x shr 8)) and 0x0000ffff
        return x
    }

    fun decodeMorton2X(num: Int): Int = compact1By1(num shr 0)
    fun decodeMorton2Y(num: Int): Int = compact1By1(num shr 1)

    fun ByteArray.deswizzle(width: Int, height: Int, bytespp: Int): ByteArray {
        val unswizzled = ByteArray(size)
        val min = min(width, height)
        val k = log(min.toDouble(), 2.0).toInt()

        for (i in 0 until width * height) {
            val x: Int
            val y: Int

            if (height < width) {
                val j = i shr (2 * k) shl (2 * k) or (decodeMorton2Y(i) and (min - 1)) shl k or (decodeMorton2X(i) and (min - 1)) shl 0
                x = j / height
                y = j % height
            } else {
                val j = i shr (2 * k) shl (2 * k) or (decodeMorton2X(i) and (min - 1)) shl k or (decodeMorton2Y(i) and (min - 1)) shl 0
                x = j % width
                y = j / width
            }

            val p = ((y * width) + x) * bytespp

            for (l in 0 until bytespp) {
                unswizzled[(p + l)] = this[(i * bytespp + l)]
            }
        }

        return unswizzled
    }

    fun BufferedImage.mapToModel(model: SRDIModel?, params: Map<String, Any?>): BufferedImage {
        if (model != null)
            return mapImageToModel(this, model, params)
        return this
    }

    fun mapImageToModel(img: BufferedImage, model: SRDIModel, params: Map<String, Any?>): BufferedImage {
        val antialias = "${params["srd:antialiasing"] ?: true}".toBoolean()
        val area = Area()
        val mesh = Area()
        
        val w = img.width
        val h = img.height
        
        model.meshes[0].faces.forEach { (one, two, three) ->
            try {
                val u1 = model.meshes[0].uvs[one]
                val u2 = model.meshes[0].uvs[two]
                val u3 = model.meshes[0].uvs[three]

                val v1 = model.meshes[0].vertices[one]
                val v2 = model.meshes[0].vertices[two]
                val v3 = model.meshes[0].vertices[three]

                area.add(Area(Polygon(intArrayOf((u1.first * w).toInt(), (u2.first * w).toInt(), (u3.first * w).toInt()), intArrayOf((u1.second * h).toInt(), (u2.second * h).toInt(), (u3.second * h).toInt()), 3)))
            } catch (ioob: IndexOutOfBoundsException) {
                debug(ioob.exportStackTrace())
                return@forEach
            }
        }

        if (antialias) {
            val newImg = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB)
            val g = newImg.createGraphics()

            g.composite = AlphaComposite.Src
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.color = Color.WHITE
            g.fill(area)

            g.composite = AlphaComposite.SrcIn
            g.drawImage(img, 0, 0, null)

            g.dispose()

            return newImg
        } else {
            val g = img.createGraphics()
            val allBut = Area(Rectangle(0, 0, img.width, img.height))
            allBut.subtract(area)

            g.clip = allBut
            g.color = Color.BLACK
            g.composite = AlphaComposite.Clear
            g.fillRect(0, 0, img.width, img.height)

            g.dispose()

            return img
        }
    }
}
