package org.abimon.spiral.core

import net.npe.tga.TGAReader
import org.abimon.spiral.core.drills.DrillHead
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.core.lin.UnknownEntry
import org.abimon.visi.io.*
import org.abimon.visi.lang.make
import org.abimon.visi.lang.times
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
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

    fun convertToBytes(format: SpiralFormat, source: DataSource): ByteArray {
        val baos = ByteArrayOutputStream()
        convert(format, source, baos)
        return baos.toByteArray()
    }
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

        if (shtx != "SHTX")
            throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format (First four bytes do not spell [SHTX], spell $shtx)")

        val version = stream.readPartialBytes(2, 2)

        when (String(version)) {
            "Fs" -> {
                val width = stream.readNumber(2, unsigned = true).toInt()
                val height = stream.readNumber(2, unsigned = true).toInt()
                val unknown = stream.readNumber(2, unsigned = true)

                val palette = ArrayList<Color>()

                for (i in 0 until 256)
                    palette.add(Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF))

                if (palette.all { it.red == 0 && it.green == 0 && it.blue == 0 && it.alpha == 0 }) {
                    println("Blank palette in Fs")

                    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    for (y in 0 until height)
                        for (x in 0 until width)
                            img.setRGB(x, y, Color(stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF, stream.read() and 0xFF).rgb)
                    return img

                } else {
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
            else -> {
            }
        }

        throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format (Reached end of function)")
    }
}

object DRVitaCompressionFormat : SpiralFormat {
    override val name: String = "Danganronpa Vita Compression"
    override val extension: String? = null

    val CMP_MAGIC = byteArrayOf(0xFC, 0xAA, 0x55, 0xA7)
    val GX3_MAGIC = byteArrayOf(0x47, 0x58, 0x33, 0x00)

    override fun isFormat(source: DataSource): Boolean = source.getInputStream().read(4) equals CMP_MAGIC

    override fun canConvert(format: SpiralFormat): Boolean = true

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.getInputStream().use { stream ->
            var magic = stream.read(4)

            if (magic equals GX3_MAGIC)
                magic = stream.read(4)

            if (magic doesntEqual CMP_MAGIC)
                throw IllegalArgumentException("${source.getLocation()} does not conform to the $name format (Magic Number ≠ ${CMP_MAGIC asBase 16}; is actually ${magic asBase 16})")

            val rawSize = stream.readNumber(4, unsigned = true)
            val compressedSize = stream.readNumber(4, unsigned = true)

            println("$rawSize -> $compressedSize")

            var i = 12
            var previousOffset = 1
            val result = ArrayList<Byte>()

            while (i < compressedSize) {
                var b = stream.read()
                i++

                val bit1 = b hasBitSet 0b10000000
                val bit2 = b hasBitSet 0b01000000
                val bit3 = b hasBitSet 0b00100000

                if (bit1) {
                    val b2 = stream.read()
                    i++

                    val count = ((b ushr 5) and 0b011) + 4
                    val offset = ((b and 0b00011111) shl 8) + b2
                    previousOffset = offset

                    (0 until count).forEach { result.add(result[result.size-offset]) }
                } else if (bit2 && bit3) {
                    val count = (b and 0b00011111)
                    val offset = previousOffset

                    (0 until count).forEach { result.add(result[result.size-offset]) }
                } else if (bit2 && !bit3) {
                    var count = (b and 0b00001111)
                    if (b hasBitSet 0b00010000) {
                        b = stream.read()
                        i++
                        count = (count shl 8) + b
                    }

                    count += 4
                    b = stream.read()
                    i++

                    (0 until count).forEach { result.add(b.toByte()) }
                } else if (!bit1 && !bit2) {
                    var count = (b and 0b00011111)
                    if (bit3) {
                        b = stream.read()
                        i++
                        count = (count shl 8) + b
                    }

                    result.addAll(stream.read(count).toList())
                    i += count
                } else
                    println("???")
            }

            if(result.size.toLong() != rawSize)
                println("Different sizes (Expected $rawSize, got ${result.size})")

            output.write(result.toByteArray())
        }
    }

    override fun convertFrom(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convertFrom(format, source, output)

    }
}

object DDS1DDSFormat : SpiralFormat {
    override val name: String = "DDS1DDS Texture"
    override val extension: String = ".dds"

    override fun isFormat(source: DataSource): Boolean = source.getInputStream().use { it.readString(8) == "DDS1DDS " }

    override fun canConvert(format: SpiralFormat): Boolean = format is PNGFormat

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream) {
        super.convert(format, source, output)

        source.getInputStream().use { stream ->
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
            println(caps2)

            val texels = ArrayList<Array<Color>>()

            (0 until ((height * width) / 16)).forEach {
                val palette = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.BLACK)

                (0 until 2).forEach {
                    val rgb565 = stream.readNumber(2, unsigned = true, little = true)
                    val r = ((rgb565 shr 11) and 0x01F) shl 3
                    val g = ((rgb565 shr 5) and 0x03F) shl 2
                    val b = (rgb565 and 0x01F) shl 3
                    palette[it] = Color(r.toInt(), g.toInt(), b.toInt())
                }

                palette[2] = Color((palette[0].red * 0.6f + palette[1].red * 0.3f) / 255.0f,
                        (palette[0].red * 0.6f + palette[1].red * 0.3f) / 255.0f,
                        (palette[0].red * 0.6f + palette[1].red * 0.3f) / 255.0f)

                palette[3] = Color((palette[0].red * 0.3f + palette[1].red * 0.6f) / 255.0f,
                        (palette[0].red * 0.3f + palette[1].red * 0.6f) / 255.0f,
                        (palette[0].red * 0.3f + palette[1].red * 0.6f) / 255.0f)

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

            val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

            val rows = ArrayList<Array<Color>>()
            val tmpRows = arrayOf(ArrayList<Color>(), ArrayList<Color>(), ArrayList<Color>(), ArrayList<Color>())

            texels.forEachIndexed { index, texel ->
                if (index % (width / 4) == 0 && tmpRows.any { it.isNotEmpty() }) {
                    tmpRows.forEach { rows.add(it.toTypedArray()); it.clear() }
                }

                texel.forEachIndexed { i, color -> tmpRows[i / 4].add(color) }
            }

            rows.forEachIndexed { y, row -> row.forEachIndexed { x, color -> img.setRGB(x, y, color.rgb) } }

//            val columns = ArrayList<Array<Color>>()
//            val tmpColumns = arrayOf(ArrayList<Color>(), ArrayList<Color>(), ArrayList<Color>(), ArrayList<Color>())
//
//            texels.forEachIndexed { index, texel ->
//                if (index % (height / 4) == 0 && tmpColumns.any { it.isNotEmpty() }) {
//                    tmpColumns.forEach { columns.add(it.toTypedArray()); it.clear() }
//                }
//
//                texel.forEachIndexed { i, color -> tmpColumns[i % 4].add(color) }
//            }
//
//            columns.forEachIndexed { x, column -> column.forEachIndexed { y, color -> img.setRGB(x, y, color.rgb) } }

            ImageIO.write(img, "PNG", output)
        }
    }
}

object UnknownFormat : SpiralFormat {
    override val name = "Unknown"
    override val extension = null

    override fun isFormat(source: DataSource): Boolean = false

    override fun canConvert(format: SpiralFormat): Boolean = false

}

object BinaryFormat : SpiralFormat {
    override val name = "Binary"
    override val extension = null

    override fun isFormat(source: DataSource): Boolean = true
    override fun canConvert(format: SpiralFormat): Boolean = true
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
    val BINARY = BinaryFormat

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
