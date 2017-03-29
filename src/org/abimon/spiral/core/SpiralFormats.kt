package org.abimon.spiral.core

import org.abimon.external.TGAReader
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.writeTo
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO

interface SpiralFormat {
    fun getName(): String
    fun getExtension(): String = getName().toLowerCase()
    fun isFormat(source: DataSource): Boolean
    fun canConvert(format: SpiralFormat): Boolean
    /**
     * Convert from this format to another
     */
    fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        if(!canConvert(format))
            throw IllegalArgumentException("Cannot convert to $format")
        if(!isFormat(source))
            throw IllegalArgumentException("${source.getLocation()} does not conform to the ${getName()} format")
    }
    fun convertFrom(format: SpiralFormat, source: DataSource, output: OutputStream) = format.convert(this, source, output)
}

class WADFormat : SpiralFormat {
    override fun getName(): String = "WAD"

    override fun isFormat(source: DataSource): Boolean {
        try {
            WAD(source)
            return true
        }
        catch(illegal: IllegalArgumentException) {}

        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is ZIPFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val wad = WAD(source)
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                wad.files.forEach {
                    zip.putNextEntry(ZipEntry(it.name))
                    it.getInputStream().writeTo(zip, closeAfter = true)
                }
                zip.close()
            }
        }
    }
}
class PAKFormat : SpiralFormat {
    override fun getName(): String = "PAK"

    override fun isFormat(source: DataSource): Boolean {
        try {
            Pak(source)
            return true
        }
        catch (e: IllegalArgumentException) { }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is ZIPFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val pak = Pak(source)
        when(format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                pak.files.forEach {
                    zip.putNextEntry(ZipEntry(it.name))
                    it.getInputStream().writeTo(zip, closeAfter = true)
                }
                zip.close()
            }
        }
    }
}
class TGAFormat : SpiralFormat {
    override fun getName(): String = "TGA"

    override fun isFormat(source: DataSource): Boolean {
        try {
            TGAReader.readImage(source.getData())
            return true
        }
        catch(e: IOException) { }
        catch(e: ArrayIndexOutOfBoundsException) { }
        catch(e: IllegalArgumentException) { }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = (format is PNGFormat) or (format is JPEGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val img = TGAReader.readImage(source.getData())
        when(format) {
            is PNGFormat -> ImageIO.write(img, "PNG", output)
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
        }
    }
}

class ZIPFormat : SpiralFormat {
    override fun getName(): String = "ZIP"

    override fun isFormat(source: DataSource): Boolean {
        try {
            val zip = ZipInputStream(source.getInputStream())
            var count = 0
            while(zip.nextEntry != null)
                count++
            zip.close()
            return count > 0
        }
        catch (e: NullPointerException) { }
        catch (e: IOException) { }

        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = false
}
class PNGFormat : SpiralFormat {
    override fun getName(): String = "PNG"

    override fun isFormat(source: DataSource): Boolean =
            source.getInputStream().use { ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                    .asSequence()
                    .any { it.formatName.toLowerCase() == "png" } }

    override fun canConvert(format: SpiralFormat): Boolean = (format is TGAFormat) or (format is JPEGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.getInputStream().use {
            when (format) {
                is TGAFormat -> output.write(ImageIO.read(it).toTGA())
                is JPEGFormat -> ImageIO.write(ImageIO.read(it).toJPG(), "JPG", output)
                else -> {}
            }
        }
    }
}
class JPEGFormat : SpiralFormat {
    override fun getName(): String = "JPG"

    override fun isFormat(source: DataSource): Boolean =
            source.getInputStream().use { ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                    .asSequence()
                    .any { (it.formatName.toLowerCase() == "jpg") or (it.formatName.toLowerCase() == "jpeg") } }

    override fun canConvert(format: SpiralFormat): Boolean = (format is TGAFormat) or (format is PNGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.getInputStream().use {
            when (format) {
                is TGAFormat -> output.write(ImageIO.read(it).toTGA())
                is PNGFormat -> ImageIO.write(ImageIO.read(it), "PNG", output)
                else -> {}
            }
        }
    }
}

class UnknownFormat : SpiralFormat {
    override fun getName(): String = "UNKNOWN"

    override fun isFormat(source: DataSource): Boolean = false

    override fun canConvert(format: SpiralFormat): Boolean = false

}

object SpiralFormats {
    val WAD = WADFormat()
    val PAK = PAKFormat()
    val TGA = TGAFormat()

    val ZIP = ZIPFormat()
    val PNG = PNGFormat()
    val JPG = JPEGFormat()

    val  UNKNOWN = UnknownFormat()

    val formats = arrayOf(WAD, PAK, TGA, ZIP, PNG, JPG)
    val drWadFormats = arrayOf(WAD, PAK, TGA)

    fun formatForExtension(extension: String): Optional<SpiralFormat> = formats.findOrEmpty { (it.getExtension() == extension) }
    fun formatForData(dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): Optional<SpiralFormat> = selectiveFormats.findOrEmpty { it.isFormat(dataSource) }
    fun formatForName(name: String): Optional<SpiralFormat> = formats.findOrEmpty { it.getName().equals(name, true) }

    fun convert(from: SpiralFormat, to: SpiralFormat, source: DataSource): ByteArray {
        val baos = ByteArrayOutputStream()
        from.convert(to, source, baos)
        return baos.toByteArray()
    }
}
