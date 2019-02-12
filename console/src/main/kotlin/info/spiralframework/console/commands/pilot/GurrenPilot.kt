package info.spiralframework.console.commands.pilot

import info.spiralframework.base.util.*
import info.spiralframework.console.Cockpit
import info.spiralframework.console.CommandBuilders
import info.spiralframework.console.commands.data.ExtractArgs
import info.spiralframework.console.imperator.CommandClass
import info.spiralframework.console.imperator.ParboiledSoldier.Companion.FAILURE
import info.spiralframework.console.imperator.ParboiledSoldier.Companion.SUCCESS
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.archives.*
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.utils.copyToStream
import org.parboiled.Action
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile
import kotlin.reflect.jvm.jvmName

@Suppress("unused")
class GurrenPilot(override val cockpit: Cockpit<*>) : CommandClass {
    companion object {
        /** Helper Variables */
        var keepLooping = AtomicBoolean(true)

        val EXTRACTABLE_ARCHIVES = arrayOf(
                AWBFormat, CpkFormat, PakFormat,
                SpcFormat, SRDFormat, WadFormat,
                ZipFormat
        )

        val PERCENT_FORMAT = DecimalFormat("00.00")
    }

    val builders = CommandBuilders(cockpit)

    /** Rules */
    val extractRule = makeRuleWith(::ExtractArgs) { argsVar ->
        Sequence(
                Localised("commands.pilot.extract.extract"),
                InlineWhitespace(),
                FirstOf(
                        Sequence(
                                Localised("commands.pilot.extract.builder"),
                                Action<Any> { argsVar.get().builder = true; true }
                        ),
                        Sequence(
                                ExistingFilePath(),
                                Action<Any> { argsVar.get().extractPath = pop() as? File; true }
                        )
                ),
                ZeroOrMore(
                        InlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        Localised("commands.pilot.extract.filter"),
                                        InlineWhitespace(),
                                        Filter(),
                                        Action<Any> { argsVar.get().filter = pop() as Regex; true }
                                ),
                                Sequence(
                                        Localised("commands.pilot.extract.dest_dir"),
                                        InlineWhitespace(),
                                        FilePath(),
                                        Action<Any> { argsVar.get().destDir = pop() as File; true }
                                ),
                                Sequence(
                                        Localised("commands.pilot.extract.leave_compressed"),
                                        Action<Any> { argsVar.get().leaveCompressed = true; true }
                                ),
                                Sequence(
                                        Localised("commands.pilot.extract.builder"),
                                        Action<Any> { argsVar.get().builder = true; true }
                                )
                        )
                )
        )
    }
    val helpRule = makeRule { Localised("commands.pilot.help") }
    val identifyRule = makeRule {
        Sequence(
                Localised("commands.pilot.identify"),
                InlineWhitespace(),
                FilePath()
        )
    }

    val exitRule = makeRule { Localised("commands.pilot.exit") }

    /** Commands */

    val help = ParboiledSoldier(helpRule) { SUCCESS }

    val identify = ParboiledSoldier(identifyRule) { stack ->
        val file = stack[0] as File

        // First thing's first - does the file even exist?
        if (!file.exists()) {
            printlnErrLocale("errors.file.doesnt_exist", file)

            return@ParboiledSoldier FAILURE
        }

        return@ParboiledSoldier SUCCESS

//        //Next up, are we dealing with a singular file?
//        if (file.isFile) {
//            //If so, we can define a data source for it here
//            //We decompress it in place, just in case it's compressed
//            val (dataSource, compressionMethods) = decompressWithFormats(file::inputStream)
//
//            //We should now have a proper data source
//            //We can now work on format identification
//            val format = SpiralFormats.formatForData(null, dataSource, file.name)
//
//            if (format != null) {
//                //The file has an identifiable format.
//
//                //Should result in something like DRVita > V3 > SPC >
//                val compressionString = if (compressionMethods.isEmpty()) "" else compressionMethods.joinToString(" > ", postfix = " > ")
//
//                //This concatenates them together, which will be something like DRVita > V3 > SPC > SRD, or just SRD if it's uncompressed
//                val formatString = "${compressionString}${format.name}"
//
//                //Print it all out
//                if (SpiralModel.tableOutput) {
//                    println(FlipTable.of(arrayOf("File", "Format"), arrayOf(arrayOf(file.absolutePath, formatString))))
//                } else {
//                    println("Identified ${file.absolutePath}")
//                    println("Format: $formatString")
//                }
//            }
//        }
    }

    val extract = ParboiledSoldier(extractRule) { stack ->
        val builderArgs = (stack[0] as ExtractArgs)//.makeImmutable(defaultFilter = ".*", defaultLeaveCompressed = false)
        if (builderArgs.builder || builderArgs.extractPath == null || builderArgs.destDir == null) {
            //Builder
            if (builderArgs.extractPath == null) {
                printLocale("commands.pilot.extract.builder.extract")
                builderArgs.extractPath = builders.filePath()
            }

            if (builderArgs.destDir == null) {
                printLocale("commands.pilot.extract.builder.dest_dir")
                builderArgs.destDir = builders.filePath()
            }

            if (builderArgs.filter == null) {
                printLocale("commands.pilot.extract.builder.filter")
                builderArgs.filter = builders.filter() ?: Regex(".*")
            }

            if (builderArgs.leaveCompressed == null) {
                printLocale("commands.pilot.extract.builder.compressed")
                builderArgs.leaveCompressed = builders.boolean() ?: false
            }
        }

        val args = builderArgs.makeImmutable(defaultFilter = Regex(".*"), defaultLeaveCompressed = false)

        if (args.extractPath == null) {
            printlnErrLocale("commands.pilot.extract.err_no_extract")

            return@ParboiledSoldier FAILURE
        }

        if (!args.extractPath.exists()) {
            printlnErrLocale("errors.files.doesnt_exist", args.extractPath)

            return@ParboiledSoldier FAILURE
        }

        if (args.destDir == null) {
            printlnErrLocale("commands.pilot.extract.err_no_dest_dir")

            return@ParboiledSoldier FAILURE
        }

        if (!args.destDir.exists() && !args.destDir.mkdirs()) {
            printlnErrLocale("errors.files.cant_create_dir", args.destDir)

            return@ParboiledSoldier FAILURE
        }

        val (dataSource, compression) = decompress(args.extractPath::inputStream)

        val result = EXTRACTABLE_ARCHIVES.map { format -> format.read(source = dataSource) }
                .filter(FormatResult<*>::didSucceed)
                .sortedBy(FormatResult<*>::chance)
                .asReversed()
                .firstOrNull()
                ?.obj

        val files: Iterator<Pair<String, InputStream>>
        val totalCount: Long

        when (result) {
            null -> {
                printlnErrLocale("commands.pilot.extract.err_no_format_for", args.extractPath)

                return@ParboiledSoldier FAILURE
            }

            is AWB -> {
                files = result.entries.iterator { entry -> entry.id.toString() to entry.inputStream }
                totalCount = result.entries.count { entry -> entry.id.toString().matches(args.filter!!) }.toLong()
            }

            is CPK -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(args.filter!!) }.toLong()
            }

            is Pak -> {
                files = result.files.iterator { entry -> entry.index.toString() to entry.inputStream }
                totalCount = result.files.count { entry -> entry.index.toString().matches(args.filter!!) }.toLong()
            }

            is SPC -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(args.filter!!) }.toLong()
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
                totalCount = entries.count { pair -> pair.first.matches(args.filter!!) }.toLong()
            }

            is WAD -> {
                files = result.files.iterator { entry -> entry.name to entry.inputStream }
                totalCount = result.files.count { entry -> entry.name.matches(args.filter!!) }.toLong()
            }

            is ZipFile -> {
                files = result.entries().iterator { entry -> entry.name to result.getInputStream(entry) }
                totalCount = result.entries().asSequence().count { entry -> entry.name.matches(args.filter!!) }.toLong()
            }

            else -> {
                printlnErrLocale("commands.pilot.extract.err_unk_format", result)

                return@ParboiledSoldier FAILURE
            }
        }

        if (compression.isEmpty())
            printlnLocale("commands.pilot.extract.archive_type", result::class.simpleName)
        else
            printlnLocale("commands.pilot.extract.compressed_archive_type", compression.joinToString(" > ") { format ->
                format::class.simpleName ?: format::class.jvmName
            }, result::class.simpleName)
        printlnLocale("commands.pilot.extract.extracting_files", totalCount, args.destDir)

        var extracted: Long = 0
        ProgressTracker(downloadingText = "commands.pilot.extract.extracting_progress", downloadedText = "commands.pilot.extract.finished") {
            trackDownload(0, totalCount)
            files.forEachFiltered({ pair -> pair.first.matches(args.filter!!) }) { (fileName, raw) ->
                val file = File(args.destDir, fileName)
                file.parentFile.mkdirs()

                raw.use { stream -> FileOutputStream(file).use(stream::copyToStream) }
                trackDownload(++extracted, totalCount)
            }
        }

        return@ParboiledSoldier SUCCESS
    }

    val exit = ParboiledSoldier(exitRule, "default") {
        printlnLocale("commands.exit.leave")
        keepLooping.set(false)

        return@ParboiledSoldier SUCCESS
    }
}