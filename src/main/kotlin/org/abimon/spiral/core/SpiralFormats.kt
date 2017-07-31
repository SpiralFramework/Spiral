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

    val formats: Array<SpiralFormat> = arrayOf(
            WADFormat, ZIPFormat,
            TGAFormat, SHTXFormat, DDS1DDSFormat, PNGFormat, JPEGFormat,
            OggFormat,
            IVFFormat, MP4Format,
            LINFormat, SpiralTextFormat,
            DRVitaCompressionFormat,
            LLFSFormat,
            GMOModelFormat,
            PAKFormat,
            TXTFormat
    )

    val audioFormats: Array<SpiralFormat> = arrayOf(OggFormat)
    val videoFormats: Array<SpiralFormat> = arrayOf(IVFFormat, MP4Format)

    val drWadFormats = arrayOf(WADFormat, TGAFormat, LINFormat, LLFSFormat, GMOModelFormat, PAKFormat)

    fun formatForExtension(extension: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.extension == extension }
    fun formatForData(dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.isFormat(dataSource) }
    fun formatForName(name: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.name.equals(name, true) }

    fun convert(from: SpiralFormat, to: SpiralFormat, source: DataSource, params: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        from.convert(to, source, baos, params)
        return baos.toByteArray()
    }
}
