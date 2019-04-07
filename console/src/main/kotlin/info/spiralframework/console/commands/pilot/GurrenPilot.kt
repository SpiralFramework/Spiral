package info.spiralframework.console.commands.pilot

import info.spiralframework.base.util.*
import info.spiralframework.base.util.copyToStream
import info.spiralframework.console.Cockpit
import info.spiralframework.console.CommandBuilders
import info.spiralframework.console.commands.data.ExtractArgs
import info.spiralframework.console.commands.shared.GurrenShared
import info.spiralframework.console.eventbus.CommandClass
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.FAILURE
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.SUCCESS
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.formats.video.SFL
import org.parboiled.Action
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.reflect.jvm.jvmName

@Suppress("unused")
class GurrenPilot(override val cockpit: Cockpit<*>) : CommandClass {
    companion object {
        /** Helper Variables */
        var keepLooping = AtomicBoolean(true)

        val PERCENT_FORMAT = DecimalFormat("00.00")
    }

    val builders = CommandBuilders(cockpit)

    /** Rules */
    val extractRule = makeRuleWith(::ExtractArgs) { argsVar ->
        Sequence(
                Localised("commands.pilot.extract.extract"),
                Action<Any> { pushMarkerSuccessBase() },
                InlineWhitespace(),
                FirstOf(
                        Sequence(
                                Localised("commands.pilot.extract.builder"),
                                Action<Any> { argsVar.get().builder = true; true }
                        ),
                        Sequence(
                                FilePath(),
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
                ),
                Action<Any> { pushMarkerSuccessCommand() }
        )
    }
    val helpRule = makeRule {
        Sequence(
                Localised("commands.pilot.help"),
                Action<Any> {
                    pushMarkerSuccessBase()
                    pushMarkerSuccessCommand()
                }
        )
    }

    val identifyRule = makeRule {
        Sequence(
                Localised("commands.pilot.identify"),
                Action<Any> { pushMarkerSuccessBase() },
                InlineWhitespace(),
                FilePath(),
                Action<Any> { pushMarkerSuccessCommand() }
        )
    }

    val exitRule = makeRule {
        Sequence(
                Localised("commands.pilot.exit"),
                Action<Any> {
                    pushMarkerSuccessBase()
                    pushMarkerSuccessCommand()
                }
        )
    }

    /** Commands */

    val help = ParboiledCommand(helpRule) { SUCCESS }

    val identify = ParboiledCommand(identifyRule) { stack ->
        val file = stack[0] as File

        // First thing's first - does the file even exist?
        if (!file.exists()) {
            printlnErrLocale("errors.file.doesnt_exist", file)

            return@ParboiledCommand FAILURE
        }

        return@ParboiledCommand SUCCESS

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

    val extract = ParboiledCommand(extractRule) { stack ->
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

            return@ParboiledCommand FAILURE
        }

        if (!args.extractPath.exists()) {
            printlnErrLocale("errors.files.doesnt_exist", args.extractPath)

            return@ParboiledCommand FAILURE
        }

        if (args.destDir == null) {
            printlnErrLocale("commands.pilot.extract.err_no_dest_dir")

            return@ParboiledCommand FAILURE
        }

        if (!args.destDir.exists() && !args.destDir.mkdirs()) {
            printlnErrLocale("errors.files.cant_create_dir", args.destDir)

            return@ParboiledCommand FAILURE
        }

        val (dataSource, compression) = decompress(args.extractPath::inputStream)

        val result = GurrenShared.EXTRACTABLE_ARCHIVES.map { format -> format.read(source = dataSource) }
                .filter(FormatResult<*>::didSucceed)
                .sortedBy(FormatResult<*>::chance)
                .asReversed()
                .firstOrNull()
                ?.obj

        if (result == null) {
            printlnErrLocale("commands.mechanic.extract.err_no_format_for", args.extractPath)

            return@ParboiledCommand FAILURE
        }

        val files: Iterator<Pair<String, InputStream>>
        val totalCount: Long

        val fileResult = GurrenShared.extractGetFilesForResult(args, result, args.filter!!)

        if (fileResult == null) {
            printlnErrLocale("commands.mechanic.extract.err_unk_format", result)

            return@ParboiledCommand FAILURE
        }

        files = fileResult.first
        totalCount = fileResult.second

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
            files.forEachFiltered({ pair -> pair.first.matches(args.filter) }) { (fileName, raw) ->
                val file = File(args.destDir, fileName)
                file.parentFile.mkdirs()

                raw.use { stream -> FileOutputStream(file).use(stream::copyToStream) }
                trackDownload(++extracted, totalCount)
            }
        }

        return@ParboiledCommand SUCCESS
    }

    val debug = ParboiledCommand(makeRule { IgnoreCase("debug") }) { stack ->
        val sfl = SFL(File("/Users/undermybrella/Workspace/KSPIRAL/shinkiro/dr1_data/Dr1/data/all/flash/fla_735/0")::inputStream)!!
        val commandTable = sfl.tables.last()

        commandTable.inputStream.use { stream ->
            stream.skip(0x10)
            while (stream.available() > 0) {
                val dataSize = stream.readInt32LE()
                val headerSize = stream.readInt16LE()
                val commandCount = stream.readInt16LE()
                val header = stream.readXBytes(max(0, headerSize - 8))
                val data = stream.readXBytes(dataSize).inputStream()

                println("Section (${header.joinToString { it.toHex() }}) | ${String(header)}")

                var currentCommand = 1
                while (data.available() > 0) {
                    val op = data.readInt16LE()
                    val paramCount = data.readInt16LE()
                    val params = IntArray(paramCount) { data.read() }

                    println("\tCommand ${currentCommand++}/$commandCount: ${op.toHex()} (${params.joinToString { it.toHex() }})")
                }
            }
        }

        return@ParboiledCommand SUCCESS
    }

    fun Number.toHex(): String = "0x${(this.toInt() and 0xFF).toString(16).toUpperCase().padStart(2, '0')}"

    val exit = ParboiledCommand(exitRule, "default") {
        printlnLocale("commands.exit.leave")
        keepLooping.set(false)

        return@ParboiledCommand SUCCESS
    }
}