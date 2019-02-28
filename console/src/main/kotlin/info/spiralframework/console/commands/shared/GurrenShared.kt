package info.spiralframework.console.commands.shared

import info.spiralframework.base.util.iterator
import info.spiralframework.console.commands.data.ExtractArgs
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.archives.*
import info.spiralframework.core.formats.video.SFLFormat
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.video.SFL
import java.io.InputStream
import java.util.zip.ZipFile

object GurrenShared {
    val EXTRACTABLE_ARCHIVES = arrayOf<ReadableSpiralFormat<out Any>>(
            AWBFormat, CpkFormat, PakFormat,
            SFLFormat,
            SpcFormat, SRDFormat, WadFormat,
            ZipFormat
    )

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