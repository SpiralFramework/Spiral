package info.spiralframework.console.jvm.commands.shared

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.core.formats.archives.*
import info.spiralframework.core.formats.audio.AudioFormats
import info.spiralframework.core.formats.compression.CrilaylaCompressionFormat
import info.spiralframework.core.formats.compression.DRVitaFormat
import info.spiralframework.core.formats.compression.DRv3CompressionFormat
import info.spiralframework.core.formats.compression.SpcCompressionFormat
import info.spiralframework.core.formats.data.DataTableStructureFormat
import info.spiralframework.core.formats.images.JPEGFormat
import info.spiralframework.core.formats.images.PNGFormat
import info.spiralframework.core.formats.images.TGAFormat
import info.spiralframework.core.formats.scripting.LinScriptFormat
import info.spiralframework.core.formats.scripting.OpenSpiralLanguageFormat
import info.spiralframework.core.formats.text.CSVFormat
import info.spiralframework.core.formats.text.UTF16TextFormat
import info.spiralframework.core.formats.text.StrictUtf8TextFormat
import info.spiralframework.formats.common.archives.*
import info.spiralframework.formats.common.archives.srd.SrdArchive
import info.spiralframework.formats.common.games.DrGame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.jvm.JVMDataSource
import java.util.zip.ZipFile
import kotlin.math.floor
import kotlin.math.log

object GurrenShared {
    val EXTRACTABLE_ARCHIVES: MutableList<ReadableSpiralFormat<out Any>> by lazy {
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
            JPEGFormat, PNGFormat, TGAFormat,
            LinScriptFormat, OpenSpiralLanguageFormat,
//                SFLFormat,
            DataTableStructureFormat
        )
    }

    val WRITABLE_FORMATS: MutableList<WritableSpiralFormat> by lazy {
        mutableListOf<WritableSpiralFormat>(
            CpkArchiveFormat, FolderFormat, PakArchiveFormat, SpcArchiveFormat, WadArchiveFormat, ZipFormat,
            AudioFormats.mp3, AudioFormats.ogg, AudioFormats.wav,
            JPEGFormat, PNGFormat, TGAFormat,
            LinScriptFormat, OpenSpiralLanguageFormat,
            CSVFormat
        )
    }

    val PREDICTIVE_FORMATS: MutableList<ReadableSpiralFormat<*>> by lazy {
        mutableListOf<ReadableSpiralFormat<*>>(
            AwbArchiveFormat, CpkArchiveFormat, SpcArchiveFormat, WadArchiveFormat, ZipFormat,
            AudioFormats.mp3, AudioFormats.ogg, AudioFormats.wav,
            CrilaylaCompressionFormat, DRVitaFormat, SpcCompressionFormat, DRv3CompressionFormat,
            JPEGFormat, PNGFormat, TGAFormat,
            LinScriptFormat, OpenSpiralLanguageFormat, UTF16TextFormat, StrictUtf8TextFormat,
            DataTableStructureFormat
        )
    }

    val CONVERTING_FORMATS: MutableMap<ReadableSpiralFormat<*>, WritableSpiralFormat> by lazy {
        mutableMapOf<ReadableSpiralFormat<*>, WritableSpiralFormat>(
            AwbArchiveFormat to ZipFormat, PakArchiveFormat to ZipFormat, SpcArchiveFormat to ZipFormat,
            TGAFormat to PNGFormat,
            LinScriptFormat to OpenSpiralLanguageFormat,
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
            .map { archiveFormat -> archiveFormat.identify(this, readContext, source) }
            .filterIsInstance<FormatResult<*, *>>()
            .filter { result -> result.confidence() >= 0.50 }
            .sortedBy(FormatResult<*, *>::confidence)
            .map(FormatResult<*, *>::format)
            .asReversed()
            .firstOrNull()
    }

    @ExperimentalCoroutinesApi
    @ExperimentalUnsignedTypes
    suspend fun getFilesForArchive(spiralContext: SpiralContext, archive: Any, regex: Regex, leaveCompressed: Boolean, predictive: Boolean, game: DrGame?, archiveName: String?): Flow<Pair<String, DataSource<*>>>? {
        when (archive) {
            is AwbArchive -> {
                val maxLen = floor(
                    log(
                        archive.files.maxBy(AwbFileEntry::id)?.id?.coerceAtLeast(1)?.toDouble()
                        ?: 1.0, 16.0
                    )
                ).toInt() + 1
                return archive.files.asFlow()
                    .filter { entry -> entry.id.toString().matches(regex) }
                    .map { file ->
                        val source = archive.openSource(file)
                        val extension: String
                        if (predictive) {
                            extension = spiralContext.predictFileType(source, game, null)?.extension ?: "dat"
                        } else {
                            extension = "dat"
                        }

                        Pair("0x${file.id.toString(16).padStart(maxLen, '0')}.$extension", source)
                    }
            }

            is CpkArchive -> {
                if (leaveCompressed) {
                    return archive.files.asFlow()
                        .filter { entry -> entry.name.matches(regex) }
                        .map { entry -> Pair(entry.name, archive.openRawSource(spiralContext, entry)) }
                } else {
                    return archive.files.asFlow()
                        .filter { entry -> entry.name.matches(regex) }
                        .transform { entry ->
                            archive.openDecompressedSource(spiralContext, entry)
                                .doOnSuccess { source -> emit(Pair(entry.name, source)) }
                        }
                }
            }

            is PakArchive -> {
                var fileNames: Array<String>? = null
                if (archiveName != null && game is DrGame.PakMapped) {
                    fileNames = game.pakNames.entries.firstOrNull { (k) -> k in archiveName }?.value

                    if (fileNames == null) {
                        val keys = game.pakNames.entries.map { (k, v) -> Pair(k.substringAfterLast('/'), v) }
                        fileNames = keys.filter { (key) -> keys.count { (k) -> k == key } == 1 }
                            .firstOrNull { (k) -> archiveName.endsWith(k) }
                            ?.second
                    }
                }

                val maxLen = floor(
                    log(
                        archive.files.maxBy(PakFileEntry::index)?.index?.coerceAtLeast(1)?.toDouble()
                        ?: 1.0, 16.0
                    )
                ).toInt() + 1
                return archive.files
                    .asFlow()
                    .filter { entry -> entry.index.toString().matches(regex) }
                    .map { file ->
                        val source = archive.openSource(file)
                        val filename: String? = fileNames?.getOrNull(file.index)
                        val extension: String
                        if (predictive && filename == null) {
                            extension = spiralContext.predictFileType(source, game, null)?.extension ?: "dat"
                        } else {
                            extension = "dat"
                        }

                        Pair(filename ?: "0x${file.index.toString(16).padStart(maxLen, '0')}.$extension", source)
                    }
            }

//            is SFL -> {
//                files = archive.tables.iterator { entry -> entry.index.toString() to entry.inputStream }
//                totalCount = archive.tables.count { entry -> entry.index.toString().matches(regex) }.toLong()
//            }

            is SpcArchive -> {
                if (leaveCompressed) {
                    return archive.files.asFlow()
                        .filter { entry -> entry.name.matches(regex) }
                        .map { entry -> Pair(entry.name, archive.openRawSource(spiralContext, entry)) }
                } else {
                    return archive.files.asFlow()
                        .filter { entry -> entry.name.matches(regex) }
                        .transform { entry ->
                            archive.openDecompressedSource(spiralContext, entry)
                                .doOnSuccess { source -> emit(Pair(entry.name, source)) }
                        }
                }
            }

            is SrdArchive -> {
                return archive.entries.groupBy { entry -> entry.classifierAsString }
                    .values
                    .map { entries ->
                        val maxLen = floor(log(entries.size.coerceAtLeast(1).toDouble(), 16.0)).toInt() + 1
                        entries.mapIndexed { index, baseSrdEntry ->
                            Pair("0x${index.toString(16).padStart(maxLen, '0')}", baseSrdEntry)
                        }
                    }
                    .flatten()
                    .asFlow()
                    .transform { (index, entry) ->
                        emit(Pair("${index}_${entry.classifierAsString}_data.dat", entry.openMainDataSource()))

                        if (entry.subDataLength > 0uL)
                            emit(Pair("${index}_${entry.classifierAsString}_subdata.dat", entry.openSubDataSource()))
                    }
            }

            is WadArchive -> {
                return archive.files.asFlow()
                    .filter { entry -> entry.name.matches(regex) }
                    .map { entry -> Pair(entry.name, archive.openSource(entry)) }
            }

            is ZipFile -> {
                return archive.entries()
                    .asIterator()
                    .asFlow()
                    .map { entry -> Pair(entry.name, JVMDataSource(location = entry.name, func = { archive.getInputStream(entry) })) }
            }

            else -> {
                return null
            }
        }
    }
}