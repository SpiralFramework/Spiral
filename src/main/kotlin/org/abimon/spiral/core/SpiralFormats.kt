package org.abimon.spiral.core

import net.npe.tga.TGAReader
import org.abimon.spiral.core.drills.DrillHead
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.core.lin.UnknownEntry
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readChunked
import org.abimon.visi.io.readPartialBytes
import org.abimon.visi.io.writeTo
import org.abimon.visi.lang.make
import org.abimon.visi.lang.times
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO

interface SpiralFormat {
    val name: String
    val extension: String?
    fun isFormat(source: DataSource): Boolean
    fun canConvert(format: SpiralFormat): Boolean
    /**
     * Convert from this format to another
     */
    fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        if (!canConvert(format))
            throw IllegalArgumentException("Cannot convert to $format")
        if (!isFormat(source))
            throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format")
    }

    fun convertFrom(format: SpiralFormat, source: DataSource, output: OutputStream) = format.convert(this, source, output)
}

object WADFormat : SpiralFormat {
    override val name = "WAD"
    override val extension = "wad"

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

object PAKFormat : SpiralFormat {
    override val name = "PAK"
    override val extension = "pak"

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

object TGAFormat : SpiralFormat {
    override val name = "TGA"
    override val extension = "tga"

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

object LINFormat : SpiralFormat {
    override val name = "LIN"
    override val extension = "lin"

    override fun isFormat(source: DataSource): Boolean {
        try {
            return Lin(source).entries.isNotEmpty()
        } catch(illegal: IllegalArgumentException) {
        }
        return false
    }

    override fun canConvert(format: SpiralFormat): Boolean = format is TXTFormat || format is SpiralTextFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        Lin(source).entries.forEach { entry ->
            if (entry is TextEntry)
                output.println("${SpiralData.opCodes[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.text.replace("\n", "\\n")}")
            else
                output.println("${SpiralData.opCodes[entry.getOpCode()]?.second ?: "0x${entry.getOpCode().toString(16)}"}|${entry.getRawArguments().joinToString()}")
        }
    }
}

object SpiralTextFormat : SpiralFormat {
    override val name = "SPIRAL Text"
    override val extension = ".sprl.txt"

    override fun isFormat(source: DataSource): Boolean = !SpiralDrill.runner.run(String(source.getData(), Charsets.UTF_8)).hasErrors()

    override fun canConvert(format: SpiralFormat): Boolean = format is LINFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        when (format) {
            is LINFormat -> {
                val lin = make<CustomLin> {
                    SpiralDrill.runner.run(String(source.getData(), Charsets.UTF_8)).valueStack.forEach { value ->
                        if (value is List<*>) (value[0] as DrillHead).formScripts(value.subList(1, value.size).filterNotNull().toTypedArray()).forEach { scriptEntry -> entry(scriptEntry) }
                    }
                }

                lin.compile(output)
            }
        }
    }
}

object ZIPFormat : SpiralFormat {
    override val name = "ZIP"
    override val extension = "zip"

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

object PNGFormat : SpiralFormat {
    override val name = "PNG"
    override val extension = "png"

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

object JPEGFormat : SpiralFormat {
    override val name = "JPEG"
    override val extension = "jpg"

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

object TXTFormat : SpiralFormat {
    override val name = "Text"
    override val extension = "txt"

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

object SHTXFormat : SpiralFormat {
    override val name = "SHTX"
    override val extension = null

    override fun isFormat(source: DataSource): Boolean = source.getInputStream().readString(4) == "SHTX"
    override fun canConvert(format: SpiralFormat): Boolean = format is TGAFormat || format is PNGFormat || format is JPEGFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        val img = toBufferedImage(source)
        when (format) {
            is PNGFormat -> ImageIO.write(img, "PNG", output)
            is JPEGFormat -> ImageIO.write(img.toJPG(), "JPG", output)
        }
    }

    /** This information is taken from BlackDragonHunt's Danganronpa-Tools */
    fun toBufferedImage(source: DataSource): BufferedImage {
        //    throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format")

        val stream = source.getInputStream()
        val shtx = stream.readString(4)

        if(shtx != "SHTX")
            throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format (First four bytes do not spell [SHTX], spell $shtx)")

        val version = stream.readPartialBytes(2, 2)

        when(String(version)) {
            "Fs" -> {
                val width = stream.readNumber(2, unsigned = true).toInt()
                val height = stream.readNumber(2, unsigned = true).toInt()
                val unknown = stream.readNumber(2, unsigned = true)

                val palette = ArrayList<Color>()

                for(i in 0 until 256)
                    palette.add(Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF))

                if(palette.all { it.red == 0 && it.green == 0 && it.blue == 0 && it.alpha == 0 }) {
                    println("Blank palette in Fs")

                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    for(y in 0 until height)
                        for(x in 0 until width)
                            img.setRGB(x, y, Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF).rgb)
                    return img

                }
                else {
                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    val pixelList = ArrayList<Color>()

                    stream.readChunked { pixels -> pixels.forEach { index -> pixelList.add(palette[index.toInt() and 0xFF]) } }

                    pixelList.forEachIndexed { index, color -> img.setRGB((index % width), (index / width), color.rgb) }

                    return img
                }
            }
            "Ff" -> {
                val width = stream.readNumber(2, unsigned = true).toInt()
                val height = stream.readNumber(2, unsigned = true).toInt()
                val unknown = stream.readNumber(2, unsigned = true)

                val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                for (y in 0 until height)
                    for (x in 0 until width)
                        img.setRGB(x, y, Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF).rgb)
                return img
            }
            else -> {}
        }

        throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format (Reached end of function)")
    }
}

object UnknownFormat : SpiralFormat {
    override val name = "Unknown"
    override val extension = null

    override fun isFormat(source: DataSource): Boolean = false

    override fun canConvert(format: SpiralFormat): Boolean = false

}

object SpiralFormats {
    val WAD = WADFormat
    val PAK = PAKFormat
    val TGA = TGAFormat
    val LIN = LINFormat

    val ZIP = ZIPFormat
    val PNG = PNGFormat
    val JPG = JPEGFormat
    val TXT = TXTFormat

    val SPRL_TXT = SpiralTextFormat

    val SHTX = SHTXFormat

    val UNKNOWN = UnknownFormat

    val formats = arrayOf(WAD, PAK, TGA, LIN, ZIP, PNG, JPG, TXT, SPRL_TXT, SHTX)
    val drWadFormats = arrayOf(WAD, PAK, TGA, LIN)

    fun formatForExtension(extension: String): SpiralFormat? = formats.firstOrNull { it.extension == extension }
    fun formatForData(dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.isFormat(dataSource) }
    fun formatForName(name: String): SpiralFormat? = formats.firstOrNull { it.name.equals(name, true) }

    fun convert(from: SpiralFormat, to: SpiralFormat, source: DataSource): ByteArray {
        val baos = ByteArrayOutputStream()
        from.convert(to, source, baos)
        return baos.toByteArray()
    }
}
