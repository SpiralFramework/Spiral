package org.abimon.spiral.core

import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.archives.*
import org.abimon.spiral.core.formats.audio.OggFormat
import org.abimon.spiral.core.formats.compression.CRILAYLAFormat
import org.abimon.spiral.core.formats.compression.DRVitaCompressionFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.formats.models.GMOModelFormat
import org.abimon.spiral.core.formats.models.OBJModelFormat
import org.abimon.spiral.core.formats.models.SRDIModelFormat
import org.abimon.spiral.core.formats.scripting.LINFormat
import org.abimon.spiral.core.formats.scripting.NonstopFormat
import org.abimon.spiral.core.formats.scripting.SFLFormat
import org.abimon.spiral.core.formats.scripting.WRDFormat
import org.abimon.spiral.core.formats.text.*
import org.abimon.spiral.core.formats.video.IVFFormat
import org.abimon.spiral.core.formats.video.MP4Format
import org.abimon.visi.io.DataSource
import java.io.ByteArrayOutputStream

object SpiralFormats {

    val formats: Array<SpiralFormat> = arrayOf(
            WADFormat, CPKFormat, ZIPFormat,
            GXTFormat, TGAFormat, SHTXFormat, DDSFormat, PNGFormat, JPEGFormat,
            OggFormat,
            IVFFormat, MP4Format,
            LINFormat, SpiralTextFormat, WRDFormat,
            DRVitaCompressionFormat, CRILAYLAFormat,
            SFLFormat,
            GMOModelFormat, OBJModelFormat, SRDIModelFormat,
            PAKFormat, SPCFormat,

            JacksonFormat.YAML, JacksonFormat.JSON,

            STXTFormat, ScriptTextFormat,
            NonstopFormat,

            TextFormat
    )

    val audioFormats: Array<SpiralFormat> = arrayOf(OggFormat, MP4Format)
    val videoFormats: Array<SpiralFormat> = arrayOf(IVFFormat, MP4Format)

    val imageFormats: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, DDSFormat, PNGFormat, JPEGFormat)

    val compressionFormats = arrayOf(DRVitaCompressionFormat, CRILAYLAFormat)

    val drArchiveFormats = arrayOf(
            WADFormat, CPKFormat,
            TGAFormat, SHTXFormat, DDSFormat,
            LINFormat,
            SFLFormat,
            GMOModelFormat,
            IVFFormat,
            OggFormat,
            PAKFormat, SPCFormat,
            NonstopFormat,
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

    fun formatForExtension(extension: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.extension?.equals(extension, true) ?: false }
    fun formatForData(dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.isFormat(dataSource) }
    fun formatForName(name: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.name.equals(name, true) } ?: if(name.equals("BINARY", true)) SpiralFormat.BinaryFormat else null

    fun formatForFile(filename: String, dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? {
        return null
    }

    fun formatForFingerprint(fingerprint: String): SpiralFormat? {
        return null
    }

    fun convert(from: SpiralFormat, to: SpiralFormat, source: DataSource, params: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        from.convert(to, source, baos, params)
        return baos.toByteArray()
    }
}
