package info.spiralframework.console.jvm.commands.shared

import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.console.jvm.commands.pilot.GurrenPilot
import info.spiralframework.core.common.formats.DefaultFormatReadContext
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.archives.AwbArchiveFormat
import info.spiralframework.core.common.formats.archives.CpkArchiveFormat
import info.spiralframework.core.common.formats.archives.PakArchiveFormat
import info.spiralframework.core.common.formats.archives.SpcArchiveFormat
import info.spiralframework.core.common.formats.archives.SrdArchiveFormat
import info.spiralframework.core.common.formats.archives.WadArchiveFormat
import info.spiralframework.core.common.formats.archives.ZipFormat
import info.spiralframework.core.common.formats.compression.CrilaylaCompressionFormat
import info.spiralframework.core.common.formats.compression.DRVitaFormat
import info.spiralframework.core.common.formats.compression.DRv3CompressionFormat
import info.spiralframework.core.common.formats.compression.SpcCompressionFormat
import info.spiralframework.core.common.formats.data.DataTableStructureFormat
import info.spiralframework.core.common.formats.images.SHTXFormat
import info.spiralframework.core.common.formats.scripting.LinScriptFormat
import info.spiralframework.core.common.formats.scripting.OpenSpiralBitcodeFormat
import info.spiralframework.core.common.formats.text.CSVFormat
import info.spiralframework.core.common.formats.text.StrictUtf8TextFormat
import info.spiralframework.core.common.formats.text.UTF16TextFormat
import info.spiralframework.core.formats.archives.*
import info.spiralframework.core.formats.audio.AudioFormats
import info.spiralframework.core.formats.images.JPEGFormat
import info.spiralframework.core.formats.images.PNGFormat
import info.spiralframework.core.formats.images.TGAFormat
import info.spiralframework.core.formats.scripting.OpenSpiralLanguageFormat
import info.spiralframework.formats.common.archives.*
import info.spiralframework.formats.common.games.DrGame

object GurrenShared {
    val EXTRACTABLE_ARCHIVES: MutableList<ReadableSpiralFormat<SpiralArchive>> by lazy {
        mutableListOf(
            AwbArchiveFormat, CpkArchiveFormat, PakArchiveFormat,
//                SFLFormat,
            SpcArchiveFormat, SrdArchiveFormat, WadArchiveFormat,
            ZipFormat
        )
    }

    val READABLE_FORMATS: MutableList<ReadableSpiralFormat<out Any>> by lazy {
        mutableListOf(
            //FolderFormat
            AwbArchiveFormat, CpkArchiveFormat, PakArchiveFormat, SpcArchiveFormat, SrdArchiveFormat, WadArchiveFormat, ZipFormat,
            AudioFormats.mp3, AudioFormats.ogg, AudioFormats.wav,
            CrilaylaCompressionFormat, DRVitaFormat, SpcCompressionFormat, DRv3CompressionFormat,
//                DDSImageFormat.DXT1,
            JPEGFormat, PNGFormat, TGAFormat, SHTXFormat,
            LinScriptFormat, OpenSpiralBitcodeFormat, OpenSpiralLanguageFormat,
//                SFLFormat,
            DataTableStructureFormat
        )
    }

    val WRITABLE_FORMATS: MutableList<WritableSpiralFormat> by lazy {
        mutableListOf<WritableSpiralFormat>(
            CpkArchiveFormat, FolderFormat, PakArchiveFormat, SpcArchiveFormat, WadArchiveFormat, ZipFormat,
            AudioFormats.mp3, AudioFormats.ogg, AudioFormats.wav,
            JPEGFormat, PNGFormat, TGAFormat,
            LinScriptFormat, OpenSpiralBitcodeFormat, OpenSpiralLanguageFormat,
            CSVFormat
        )
    }

    val PREDICTIVE_FORMATS: MutableList<ReadableSpiralFormat<*>> by lazy {
        mutableListOf<ReadableSpiralFormat<*>>(
            AwbArchiveFormat, CpkArchiveFormat, SpcArchiveFormat, WadArchiveFormat, ZipFormat,
            AudioFormats.mp3, AudioFormats.ogg, AudioFormats.wav,
            CrilaylaCompressionFormat, DRVitaFormat, SpcCompressionFormat, DRv3CompressionFormat,
            JPEGFormat, PNGFormat, TGAFormat,
            LinScriptFormat, OpenSpiralBitcodeFormat, UTF16TextFormat, StrictUtf8TextFormat,
            DataTableStructureFormat
        )
    }

    val CONVERTING_FORMATS: MutableMap<ReadableSpiralFormat<*>, WritableSpiralFormat> by lazy {
        mutableMapOf<ReadableSpiralFormat<*>, WritableSpiralFormat>(
            AwbArchiveFormat to ZipFormat, PakArchiveFormat to ZipFormat, SpcArchiveFormat to ZipFormat,
            TGAFormat to PNGFormat,
            LinScriptFormat to OpenSpiralBitcodeFormat,
            DataTableStructureFormat to CSVFormat
        )
    }

    suspend fun showEnvironment(context: SpiralContext) {
        with(context) {
            println(
                retrieveEnvironment().entries
                    .groupBy { (k) -> k.substringBeforeLast('.') }
                    .entries
                    .sortedBy(Map.Entry<String, *>::key)
                    .flatMap { (_, v) -> v.sortedBy(Map.Entry<String, String>::key) }
                    .joinToString("\n") { (k, v) ->
                        "environment[$k]: \"${v.replace("\r", "\\r").replace("\n", "\\n")}\""
                    }
            )
        }
    }

//    val DR_GAMES = arrayOf(Dr1, DR2, UDG, V3, UnknownHopesPeakGame)

    suspend fun SpiralContext.predictFileType(source: DataSource<*>, game: DrGame?, name: String?): SpiralFormat? {
        val readContext = DefaultFormatReadContext(name, game)

        return PREDICTIVE_FORMATS
            .map { archiveFormat -> archiveFormat.identify(this, GurrenPilot.formatContext.withOptional(DrGame, game).withOptional(ISpiralProperty.FileName, name), source) }
            .filterIsInstance<FormatResult<*, *>>()
            .filter { result -> result.confidence() >= 0.50 }
            .sortedBy(FormatResult<*, *>::confidence)
            .map(FormatResult<*, *>::format)
            .asReversed()
            .firstOrNull()
    }
}