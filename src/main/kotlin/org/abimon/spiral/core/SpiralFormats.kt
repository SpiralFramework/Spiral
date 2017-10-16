package org.abimon.spiral.core

import org.abimon.spiral.core.data.CacheHandler
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
            WADFormat, CPKFormat, ZIPFormat,
            TGAFormat, SHTXFormat, DDSFormat, PNGFormat, JPEGFormat,
            OggFormat,
            IVFFormat, MP4Format,
            LINFormat, SpiralTextFormat, WRDFormat,
            DRVitaCompressionFormat, CRILAYLAFormat,
            LLFSFormat,
            GMOModelFormat,
            PAKFormat, SPCFormat,

            TXTFormat
    )

    val audioFormats: Array<SpiralFormat> = arrayOf(OggFormat, MP4Format)
    val videoFormats: Array<SpiralFormat> = arrayOf(IVFFormat, MP4Format)

    val imageFormats: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, DDSFormat, PNGFormat, JPEGFormat)

    val compressionFormats = arrayOf(DRVitaCompressionFormat, CRILAYLAFormat)

    val drArchiveFormats = arrayOf(
            WADFormat, CPKFormat,
            TGAFormat, SHTXFormat, DDSFormat,
            LINFormat,
            LLFSFormat,
            GMOModelFormat,
            IVFFormat,
            OggFormat,
            NonstopFormat,
            PAKFormat, SPCFormat,
            DRVitaCompressionFormat, CRILAYLAFormat
    )

    fun isCompressed(dataSource: DataSource): Boolean = compressionFormats.any { format -> format.isFormat(dataSource) }
    fun decompress(dataSource: DataSource): DataSource {
        val compressionFormat = compressionFormats.firstOrNull { format -> format.isFormat(dataSource) } ?: return dataSource
        val (output, source) = CacheHandler.cacheStream()
        compressionFormat.convert(SpiralFormat.BinaryFormat, dataSource, output, emptyMap())
        return source
    }
    fun decompressFully(dataSource: DataSource): DataSource {
        var data: DataSource = dataSource
        while(true) {
            val compressionFormat = compressionFormats.firstOrNull { format -> format.isFormat(data) } ?: return data
            val (output, source) = CacheHandler.cacheStream()
            compressionFormat.convert(SpiralFormat.BinaryFormat, data, output, emptyMap())
            data = source
        }
    }

    fun formatForExtension(extension: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.extension == extension }
    fun formatForData(dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.isFormat(dataSource) }
    fun formatForName(name: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.name.equals(name, true) } ?: if(name.equals("BINARY", true)) SpiralFormat.BinaryFormat else null

    fun convert(from: SpiralFormat, to: SpiralFormat, source: DataSource, params: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        from.convert(to, source, baos, params)
        return baos.toByteArray()
    }
}
