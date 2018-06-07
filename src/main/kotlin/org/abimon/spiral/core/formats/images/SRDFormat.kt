package org.abimon.spiral.core.formats.images

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.archives.SPCFormat
import org.abimon.spiral.core.formats.archives.ZIPFormat
import org.abimon.spiral.core.objects.archives.SPC
import org.abimon.spiral.core.objects.archives.SPCEntry
import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.objects.archives.srd.TXREntry
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.core.objects.models.SRDIModel
import org.abimon.spiral.util.readTexture
import org.abimon.visi.collections.remove
import org.abimon.visi.lang.and
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
        SpiralFormat[V3 to SPCFormat and PNGFormat] = this::convertFromArchive
        SpiralFormat[V3 to SPCFormat and ZIPFormat] = this::convertFromArchive
    }

    fun convertFromArchive(game: DRGame?, from: SpiralFormat, to: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(!"${params["srd:convert"] ?: true}".toBoolean())
            return false

        val otherEntries: MutableMap<String, () -> InputStream> = HashMap()
        val images: MutableMap<String, BufferedImage> = HashMap()
        val mapToModels: Boolean = "${params["srd:mapToModels"] ?: true}".toBoolean()
        var imageOverride = false

        when (from) {
            SPCFormat -> {
                val spc = SPC(dataSource)

                val files = spc.files.groupBy { entry -> entry.name.endsWith("srd") }
                val srds = files[true] ?: emptyList()
                val others = files[false]!!.toMutableList()

                srds.forEach { srdEntry ->
                    val img: SPCEntry

                    if (others.any { entry -> entry.name == srdEntry.name.replaceAfterLast('.', "srdv") })
                        img = others.remove { entry -> entry.name == srdEntry.name.replaceAfterLast('.', "srdv") } ?: return@forEach
                    else
                        img = others.firstOrNull { entry -> entry.name == srdEntry.name.replaceAfterLast('.', "srdi") } ?: run { SpiralData.LOGGER.debug("No such element for {}", srdEntry.name.substringBeforeLast('.')); otherEntries[srdEntry.name] = srdEntry::inputStream; return@forEach }

                    val model: SPCEntry? = if (mapToModels) others.firstOrNull { entry -> entry.name == srdEntry.name.split('.')[0] + ".srdi" } else null

                    val before = otherEntries.size
                    //readSRD(srdEntry::inputStream, img::inputStream, if (model != null) SRDIModel(SRD(srdEntry::inputStream), model::inputStream) else null, otherEntries, images, params)

                    val srd = SRD(srdEntry::inputStream)
                    srd.entries.filterIsInstance(TXREntry::class.java).forEach txrLoop@{ txr ->
                        images[txr.rsiEntry.name] = txr.readTexture(img::inputStream) ?: return@txrLoop
                    }



                    if (otherEntries.size != before)
                        imageOverride = true
                }

                others.forEach { entry -> otherEntries[entry.name] = entry::inputStream }
            }
            else -> throw IllegalArgumentException("Unknown archive to convert from!")
        }

        if (!imageOverride && images.isEmpty() && !SpiralData.LOGGER.isTraceEnabled)
            return false

        val format = SpiralFormats.formatForName(params["srd:format"]?.toString() ?: "PNG", SpiralFormats.imageFormats)
                ?: PNGFormat
        when (to) {
            is PNGFormat -> ImageIO.write(images.entries.first().value, "PNG", output)
            is ZIPFormat -> {
                val zos = ZipOutputStream(output)

                otherEntries.forEach { entryName, data ->
                    zos.putNextEntry(ZipEntry(entryName))
                    data().use { stream -> stream.copyTo(zos) }
                }

                images.forEach { imageName, image ->
                    zos.putNextEntry(ZipEntry(imageName.replaceAfterLast('.', format.extension!!)))
                    if(format == PNGFormat)
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
                SpiralData.LOGGER.debug("An error occurred while mapping {} to {}: ", img, model, ioob)
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
