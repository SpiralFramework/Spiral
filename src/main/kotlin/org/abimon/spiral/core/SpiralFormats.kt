package org.abimon.spiral.core

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.archives.*
import org.abimon.spiral.core.formats.audio.OggFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.formats.models.ColladaModelFormat
import org.abimon.spiral.core.formats.models.GMOModelFormat
import org.abimon.spiral.core.formats.models.OBJModelFormat
import org.abimon.spiral.core.formats.scripting.*
import org.abimon.spiral.core.formats.text.JacksonFormat
import org.abimon.spiral.core.formats.text.STXTFormat
import org.abimon.spiral.core.formats.text.SpiralTextFormat
import org.abimon.spiral.core.formats.text.TextFormat
import org.abimon.spiral.core.formats.video.IVFFormat
import org.abimon.spiral.core.formats.video.MP4Format
import org.abimon.spiral.core.objects.compression.CRILAYLACompression
import org.abimon.spiral.core.objects.compression.DRVitaCompression
import org.abimon.spiral.core.objects.compression.ICompression
import org.abimon.spiral.core.objects.compression.V3Compression
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.extension
import java.io.InputStream

object SpiralFormats {

    var formats: Array<SpiralFormat> = arrayOf(
            WADFormat, CPKFormat, ZIPFormat,
            GXTFormat, TGAFormat, SHTXFormat, DDSFormat, PNGFormat, JPEGFormat,
            OggFormat,
            IVFFormat, MP4Format,
            LINFormat, SpiralTextFormat, WRDFormat,
            SFLFormat,
            GMOModelFormat, OBJModelFormat, ColladaModelFormat,
            PakBGFormats,
            PAKFormat, SPCFormat,

            JacksonFormat.YAML, JacksonFormat.JSON,

            STXTFormat,
            OpenSpiralLanguageFormat,
            NonstopFormat,

            TextFormat
    )

    var audioFormats: Array<SpiralFormat> = arrayOf(OggFormat, MP4Format)
    var videoFormats: Array<SpiralFormat> = arrayOf(IVFFormat, MP4Format)

    var imageFormats: Array<SpiralFormat> = arrayOf(TGAFormat, SHTXFormat, DDSFormat, PNGFormat, JPEGFormat)

    var drArchiveFormats = arrayOf(
            WADFormat, CPKFormat,
            TGAFormat, SHTXFormat, DDSFormat,
            LINFormat,
            SFLFormat,
            GMOModelFormat,
            IVFFormat,
            OggFormat,
            PAKFormat, SPCFormat,
            PakBGFormats,
            NonstopFormat
    )

    var drWadFormats = arrayOf(
            TGAFormat,
            LINFormat,
            SFLFormat,
            GMOModelFormat,
            IVFFormat,
            OggFormat,
            PAKFormat
    )

    var gameRequiredFormats = arrayOf(
            LINFormat,
            WRDFormat,
            NonstopFormat
    )

    var gameAmbiguousFormats = formats.filterNot { format -> format in gameRequiredFormats }.toTypedArray()

    fun nullContext(name: String): (() -> InputStream)? = null

    var compressionMethods: Array<ICompression> = arrayOf(
            V3Compression, CRILAYLACompression, DRVitaCompression
    )

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
