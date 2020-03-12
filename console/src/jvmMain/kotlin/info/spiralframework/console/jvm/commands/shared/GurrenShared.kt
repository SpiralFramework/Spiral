package info.spiralframework.console.jvm.commands.shared

import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
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

//    val DR_GAMES = arrayOf(Dr1, DR2, UDG, V3, UnknownHopesPeakGame)

//    fun extractGetFilesForResult(args: ExtractArgs.Immutable, result: Any, regex: Regex): Pair<Iterator<Pair<String, InputStream>>, Long>? {
//        val files: Iterator<Pair<String, InputStream>>
//        val totalCount: Long
//
//        when (result) {
//            is AWB -> {
//                files = result.entries.iterator { entry -> entry.id.toString() to entry.inputStream }
//                totalCount = result.entries.count { entry -> entry.id.toString().matches(regex) }.toLong()
//            }
//
//            is CPK -> {
//                files = if (args.leaveCompressed!!) {
//                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
//                } else {
//                    result.files.iterator { entry -> entry.name to entry.inputStream }
//                }
//                totalCount = result.files.count { entry -> entry.name.matches(regex) }.toLong()
//            }
//
//            is Pak -> {
//                files = result.files.iterator { entry -> entry.index.toString() to entry.inputStream }
//                totalCount = result.files.count { entry -> entry.index.toString().matches(regex) }.toLong()
//            }
//
//            is SFL -> {
//                files = result.tables.iterator { entry -> entry.index.toString() to entry.inputStream }
//                totalCount = result.tables.count { entry -> entry.index.toString().matches(regex) }.toLong()
//            }
//
//            is SPC -> {
//                files = if (args.leaveCompressed!!) {
//                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
//                } else {
//                    result.files.iterator { entry -> entry.name to entry.inputStream }
//                }
//                totalCount = result.files.count { entry -> entry.name.matches(regex) }.toLong()
//            }
//
//            is SRD -> {
//                val entries = result.entries.groupBy { entry -> entry.dataType }
//                        .values.map { entries ->
//                    entries.mapIndexed { index, entry ->
//                        listOf(
//                                "$index-${entry.dataType}-data.dat" to entry::dataStream,
//                                "$index-${entry.dataType}-subdata.dat" to entry::subdataStream
//                        )
//                    }.flatten()
//                }.flatten()
//                files = entries.iterator { pair -> pair.first to pair.second() }
//                totalCount = entries.count { pair -> pair.first.matches(regex) }.toLong()
//            }
//
//            is WAD -> {
//                files = result.files.iterator { entry -> entry.name to entry.inputStream }
//                totalCount = result.files.count { entry -> entry.name.matches(regex) }.toLong()
//            }
//
//            is ZipFile -> {
//                files = result.entries().iterator().map { entry -> entry.name to result.getInputStream(entry) }
//                totalCount = result.entries().asSequence().count { entry -> entry.name.matches(regex) }.toLong()
//            }
//
//            else -> {
//                return null
//            }
//        }
//
//        return files to totalCount
//    }
}