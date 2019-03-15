package info.spiralframework.console.commands.shared

import info.spiralframework.base.util.iterator
import info.spiralframework.console.commands.data.ExtractArgs
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.core.formats.archives.*
import info.spiralframework.core.formats.audio.AudioFormats
import info.spiralframework.core.formats.compression.CRILAYLAFormat
import info.spiralframework.core.formats.compression.DRVitaFormat
import info.spiralframework.core.formats.compression.SPCCompressionFormat
import info.spiralframework.core.formats.compression.V3CompressionFormat
import info.spiralframework.core.formats.images.*
import info.spiralframework.core.formats.scripting.LinFormat
import info.spiralframework.core.formats.scripting.OpenSpiralLanguageFormat
import info.spiralframework.core.formats.scripting.WordScriptFormat
import info.spiralframework.core.formats.video.SFLFormat
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.video.SFL
import java.io.InputStream
import java.util.zip.ZipFile

object GurrenShared {
    val EXTRACTABLE_ARCHIVES: MutableList<ReadableSpiralFormat<out Any>> by lazy {
        mutableListOf(
                AWBFormat, CpkFormat, PakFormat,
                SFLFormat,
                SpcFormat, SRDFormat, WadFormat,
                ZipFormat
        )
    }

    val READABLE_FORMATS: MutableList<ReadableSpiralFormat<out Any>> by lazy {
        mutableListOf(
                //FolderFormat
                AWBFormat, CpkFormat, PakFormat, SpcFormat, SRDFormat, WadFormat, ZipFormat,
                AudioFormats.mp3, AudioFormats.ogg, AudioFormats.wav,
                CRILAYLAFormat, DRVitaFormat, SPCCompressionFormat, V3CompressionFormat,
                DDSImageFormat.DXT1,
                JPEGFormat, PNGFormat, SHTXFormat, TGAFormat,
                LinFormat, WordScriptFormat, OpenSpiralLanguageFormat,
                SFLFormat
        )
    }

    val WRITABLE_FORMATS: MutableList<WritableSpiralFormat> by lazy {
        mutableListOf<WritableSpiralFormat>(
                CpkFormat, FolderFormat, PakFormat, SpcFormat, WadFormat, ZipFormat,
                AudioFormats.mp3, AudioFormats.ogg, AudioFormats.wav,
                JPEGFormat, PNGFormat, SHTXFormat, TGAFormat,
                LinFormat, WordScriptFormat, OpenSpiralLanguageFormat
        )
    }

    fun extractGetFilesForResult(args: ExtractArgs.Immutable, result: Any, regex: Regex): Pair<Iterator<Pair<String, InputStream>>, Long>? {
        val files: Iterator<Pair<String, InputStream>>
        val totalCount: Long

        when (result) {
            is AWB -> {
                files = result.entries.iterator { entry -> entry.id.toString() to entry.inputStream }
                totalCount = result.entries.count { entry -> entry.id.toString().matches(regex) }.toLong()
            }

            is CPK -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }.toLong()
            }

            is Pak -> {
                files = result.files.iterator { entry -> entry.index.toString() to entry.inputStream }
                totalCount = result.files.count { entry -> entry.index.toString().matches(regex) }.toLong()
            }

            is SFL -> {
                files = result.tables.iterator { entry -> entry.index.toString() to entry.inputStream }
                totalCount = result.tables.count { entry -> entry.index.toString().matches(regex) }.toLong()
            }

            is SPC -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }.toLong()
            }

            is SRD -> {
                val entries = result.entries.groupBy { entry -> entry.dataType }
                        .values.map { entries ->
                    entries.mapIndexed { index, entry ->
                        listOf(
                                "$index-${entry.dataType}-data.dat" to entry::dataStream,
                                "$index-${entry.dataType}-subdata.dat" to entry::subdataStream
                        )
                    }.flatten()
                }.flatten()
                files = entries.iterator { pair -> pair.first to pair.second() }
                totalCount = entries.count { pair -> pair.first.matches(regex) }.toLong()
            }

            is WAD -> {
                files = result.files.iterator { entry -> entry.name to entry.inputStream }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }.toLong()
            }

            is ZipFile -> {
                files = result.entries().iterator { entry -> entry.name to result.getInputStream(entry) }
                totalCount = result.entries().asSequence().count { entry -> entry.name.matches(regex) }.toLong()
            }

            else -> {
                return null
            }
        }

        return files to totalCount
    }
}