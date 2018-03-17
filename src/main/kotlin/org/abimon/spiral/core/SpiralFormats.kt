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
import org.abimon.spiral.core.formats.scripting.*
import org.abimon.spiral.core.formats.text.*
import org.abimon.spiral.core.formats.video.IVFFormat
import org.abimon.spiral.core.formats.video.MP4Format
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.extension
import java.io.InputStream

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
            PakBGFormats,
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
            PakBGFormats,
            NonstopFormat,
            DRVitaCompressionFormat, CRILAYLAFormat
    )

    val drWadFormats = arrayOf(
            TGAFormat,
            LINFormat,
            SFLFormat,
            GMOModelFormat,
            IVFFormat,
            OggFormat,
            PAKFormat
    )

    val gameRequiredFormats = arrayOf(
            LINFormat,
            WRDFormat,
            NonstopFormat
    )

    val gameAmbiguousFormats = formats.filterNot { format -> format in gameRequiredFormats }.toTypedArray()

    fun nullContext(name: String): (() -> InputStream)? = null

    //TODO: Use an actual game/context
    fun isCompressed(dataSource: DataSource): Boolean = compressionFormats.any { format -> format.isFormat(null, dataSource.location, this::nullContext, dataSource::inputStream) }
    //TODO: Use an actual game/context
    fun decompress(dataSource: () -> InputStream): () -> InputStream {
        val compressionFormat = compressionFormats.firstOrNull { format -> format.isFormat(null, null, this::nullContext, dataSource) } ?: return dataSource
        val (output, source) = CacheHandler.cacheStream()
        compressionFormat.convert(null, SpiralFormat.BinaryFormat, null, this::nullContext, dataSource, output, emptyMap())
        return source
    }
    //TODO: Use an actual game/context
    fun decompressFully(dataSource: () -> InputStream): () -> InputStream {
        var data: () -> InputStream = dataSource
        while(true) {
            val compressionFormat = compressionFormats.firstOrNull { format -> format.isFormat(null, null, this::nullContext, data) } ?: return data
            val (output, source) = CacheHandler.cacheStream()
            compressionFormat.convert(null, SpiralFormat.BinaryFormat, null, this::nullContext, data, output, emptyMap())
            data = source
        }
    }

    fun formatForExtension(extension: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { format -> format.extension?.equals(extension, true) ?: false }
    @JvmOverloads
    fun formatForData(game: DRGame?, dataSource: () -> InputStream, name: String? = null, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.map { format -> format to format.isFormatWithConfidence(game, name, this::nullContext, dataSource) }
            .filter { (_, isFormat) -> isFormat.first }
            .sortedBy { (_, confidence) -> confidence.second }
            .lastOrNull()?.first

    fun formatForName(name: String, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? = selectiveFormats.firstOrNull { it.name.equals(name, true) } ?: if(name.equals("BINARY", true)) SpiralFormat.BinaryFormat else null

    fun formatForNameAndData(name: String, dataSource: () -> InputStream, game: DRGame? = null, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? {
        val nameByExtension = formatForExtension(name.extension)

        if(nameByExtension?.isFormat(game, name, this::nullContext, dataSource) == true)
            return nameByExtension

        return selectiveFormats.map { format -> format to format.isFormatWithConfidence(null, name, this::nullContext, dataSource) }
                .filter { (_, isFormat) -> isFormat.first }
                .sortedBy { (_, confidence) -> confidence.second }
                .lastOrNull()?.first
    }

    fun formatForFile(filename: String, dataSource: DataSource, selectiveFormats: Array<SpiralFormat> = formats): SpiralFormat? {
        return null
    }

    fun formatForFingerprint(fingerprint: String): SpiralFormat? {
        return null
    }
}
