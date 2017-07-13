package org.abimon.spiral.core

import org.abimon.spiral.core.formats.*
import org.abimon.visi.io.DataSource
import java.io.ByteArrayOutputStream

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

    val UNKNOWN = SpiralFormat.UnknownFormat
    val BINARY = SpiralFormat.BinaryFormat

    val formats = arrayOf(WADFormat, PAKFormat, TGAFormat, LINFormat, ZIPFormat, PNGFormat, JPEGFormat, TXTFormat, SpiralTextFormat, SHTXFormat, DRVitaCompressionFormat, DDS1DDSFormat, LLFSFormat, GMOModelFormat)
    val drWadFormats = arrayOf(WADFormat, PAKFormat, TGAFormat, LINFormat, LLFSFormat, GMOModelFormat)

    fun formatForExtension(extension: String): SpiralFormat? = formats.firstOrNull { it.extension == extension }
    fun formatForData(dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.isFormat(dataSource) }
    fun formatForName(name: String): SpiralFormat? = formats.firstOrNull { it.name.equals(name, true) }

    fun convert(from: SpiralFormat, to: SpiralFormat, source: DataSource): ByteArray {
        val baos = ByteArrayOutputStream()
        from.convert(to, source, baos)
        return baos.toByteArray()
    }
}
