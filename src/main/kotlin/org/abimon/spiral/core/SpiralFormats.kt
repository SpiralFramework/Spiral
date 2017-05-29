package org.abimon.spiral.core

import org.abimon.external.TGAReader
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.core.lin.UnknownEntry
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.writeTo
import org.abimon.visi.lang.make
import org.abimon.visi.lang.times
import java.io.*
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
        if (!canConvert(format))
            throw IllegalArgumentException("Cannot convert to $format")
        if (!isFormat(source))
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
        } catch(illegal: IllegalArgumentException) {
        }

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
        } catch (e: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is ZIPFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val pak = Pak(source)
        when (format) {
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
        } catch(e: IOException) {
        } catch(e: ArrayIndexOutOfBoundsException) {
        } catch(e: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = (format is PNGFormat) or (format is JPEGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val img = TGAReader.readImage(source.getData())
        when (format) {
            is PNGFormat -> ImageIO.write(img, "PNG", output)
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
        }
    }
}

class LINFormat : SpiralFormat {
    override fun getName(): String = "LIN"

    override fun isFormat(source: DataSource): Boolean {
        try {
            Lin(source)
            return true
        } catch(illegal: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is TXTFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        Lin(source).entries.forEach { entry ->
            if (entry is TextEntry)
                output.writeString("${SpiralData.opCodes[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.text.replace("\n", "\\n")}\n")
            else
                output.writeString("${SpiralData.opCodes[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.getRawArguments().joinToString()}\n")
        }
    }
}

class ZIPFormat : SpiralFormat {
    override fun getName(): String = "ZIP"

    override fun isFormat(source: DataSource): Boolean {
        try {
            val zip = ZipInputStream(source.getInputStream())
            var count = 0
            while (zip.nextEntry != null)
                count++
            zip.close()
            return count > 0
        } catch (e: NullPointerException) {
        } catch (e: IOException) {
        }

        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is PAKFormat || format is WADFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        when (format) {
            is PAKFormat -> customPak(source).compile(output)
            else -> TODO("NYI PAK -> ${format::class.simpleName}")
        }
    }
}

class PNGFormat : SpiralFormat {
    override fun getName(): String = "PNG"

    override fun isFormat(source: DataSource): Boolean =
            source.getInputStream().use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { it.formatName.toLowerCase() == "png" }
            }

    override fun canConvert(format: SpiralFormat): Boolean = (format is TGAFormat) or (format is JPEGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.getInputStream().use {
            when (format) {
                is TGAFormat -> output.write(ImageIO.read(it).toTGA())
                is JPEGFormat -> ImageIO.write(ImageIO.read(it).toJPG(), "JPG", output)
                else -> {
                }
            }
        }
    }
}

class JPEGFormat : SpiralFormat {
    override fun getName(): String = "JPG"

    override fun isFormat(source: DataSource): Boolean =
            source.getInputStream().use {
                ImageIO.getImageReaders(ImageIO.createImageInputStream(it))
                        .asSequence()
                        .any { (it.formatName.toLowerCase() == "jpg") or (it.formatName.toLowerCase() == "jpeg") }
            }

    override fun canConvert(format: SpiralFormat): Boolean = (format is TGAFormat) or (format is PNGFormat)

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.getInputStream().use {
            when (format) {
                is TGAFormat -> output.write(ImageIO.read(it).toTGA())
                is PNGFormat -> ImageIO.write(ImageIO.read(it), "PNG", output)
                else -> {
                }
            }
        }
    }
}

class TXTFormat : SpiralFormat {
    override fun getName(): String = "TXT"

    override fun isFormat(source: DataSource): Boolean = true

    override fun canConvert(format: SpiralFormat): Boolean = format is LINFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        if (isDebug) println("Begun Converting\n${"-" * 100}")
        when (format) {
            is LINFormat -> {
                val reader = BufferedReader(InputStreamReader(source.getInputStream()))
                val lin = make<CustomLin> {
                    reader.forEachLine loop@ { line ->
                        val parts = line.split("|", limit = 2)
                        if (parts.size != 2) {
                            return@loop
                        }

                        val opCode = parts[0]

                        val op: Int
                        if (opCode.startsWith("0x"))
                            op = opCode.substring(2).toInt(16)
                        else if (opCode.matches("\\d+".toRegex()))
                            op = opCode.toInt()
                        else if (SpiralData.opCodes.values.any { (_, name) -> name.equals(opCode, true) })
                            op = SpiralData.opCodes.entries.first { (_, pair) -> pair.second.equals(opCode, true) }.key
                        else
                            op = 0x00

                        if (op == 2) { //Text
                            entry(parts[1])
                        } else {
                            val args = if (parts[1].isBlank()) IntArray(0) else parts[1].split(",").map(String::trim).map(String::toInt).toIntArray()
                            when (op) {
                                0x00 -> entry(TextCountEntry((args[1] shl 8) or args[0]))
                                else -> entry(UnknownEntry(op, args))
                            }
                        }
                    }
                }
                lin.compile(output)
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
    val LIN = LINFormat()

    val ZIP = ZIPFormat()
    val PNG = PNGFormat()
    val JPG = JPEGFormat()
    val TXT = TXTFormat()

    val UNKNOWN = UnknownFormat()

    val formats = arrayOf(WAD, PAK, TGA, LIN, ZIP, PNG, JPG, TXT)
    val drWadFormats = arrayOf(WAD, PAK, TGA, LIN)

    fun formatForExtension(extension: String): Optional<SpiralFormat> = formats.findOrEmpty { (it.getExtension() == extension) }
    fun formatForData(dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): Optional<SpiralFormat> = selectiveFormats.findOrEmpty { it.isFormat(dataSource) }
    fun formatForName(name: String): Optional<SpiralFormat> = formats.findOrEmpty { it.getName().equals(name, true) }

    fun convert(from: SpiralFormat, to: SpiralFormat, source: DataSource): ByteArray {
        val baos = ByteArrayOutputStream()
        from.convert(to, source, baos)
        return baos.toByteArray()
    }
}
