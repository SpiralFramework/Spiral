package info.spiralframework.console.commands

import info.spiralframework.base.util.forEachFiltered
import info.spiralframework.base.util.iterator
import info.spiralframework.base.util.printlnErrLocale
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.console.Cockpit
import info.spiralframework.console.data.mechanic.ExtractArgs
import info.spiralframework.console.imperator.CommandClass
import info.spiralframework.console.imperator.ParboiledSoldier.Companion.FAILURE
import info.spiralframework.console.imperator.ParboiledSoldier.Companion.SUCCESS
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.archives.*
import info.spiralframework.formats.archives.*
import info.spiralframework.formats.utils.copyToStream
import org.parboiled.Action
import org.parboiled.support.Var
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.util.zip.ZipFile
import kotlin.reflect.jvm.jvmName

@Suppress("unused")
class Gurren(override val cockpit: Cockpit<*>) : CommandClass {
    companion object {
        val EXTRACTABLE_ARCHIVES = arrayOf(
                AWBFormat, CpkFormat, PakFormat,
                SpcFormat, SRDFormat, WadFormat,
                ZipFormat
        )

        val PERCENT_FORMAT = DecimalFormat("00.00")
    }

    /** Helper Variables */
    var keepLooping = true

    /** Rules */
    val extractRule = makeRule {
        val argsVar = Var<ExtractArgs>()
        Sequence(
                Action<Any> { argsVar.set(ExtractArgs()) },
                Localised("commands.extract.extract"),
                InlineWhitespace(),
                ExistingFilePath(),
                Action<Any> { argsVar.get().extractPath = pop() as? File; true },
                ZeroOrMore(
                        InlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        Localised("commands.extract.filter"),
                                        InlineWhitespace(),
                                        ParameterNoEscapes(),
                                        Action<Any> { argsVar.get().filter = pop() as? String; true }
                                ),
                                Sequence(
                                        Localised("commands.extract.dest_dir"),
                                        InlineWhitespace(),
                                        FilePath(),
                                        Action<Any> { argsVar.get().destDir = pop() as? File; true }
                                ),
                                Sequence(
                                        Localised("commands.extract.leave_compressed"),
                                        Action<Any> { argsVar.get().leaveCompressed = true; true }
                                )
                        )
                ),
                Action<Any> { push(argsVar.get()) }
        )
    }

    val helpRule = makeRule { Localised("commands.help") }

    val identifyRule = makeRule {
        Sequence(
                Localised("commands.identify"),
                InlineWhitespace(),
                FilePath()
        )
    }

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
        val args = (stack[0] as ExtractArgs).makeImmutable(defaultFilter = ".*", defaultLeaveCompressed = false)
        val regex = args.filter!!.toRegex()

        if (args.extractPath == null) {
            printlnErrLocale("commands.extract.err_no_extract")

            return@ParboiledSoldier FAILURE
        }

        if (!args.extractPath.exists()) {
            printlnErrLocale("errors.files.doesnt_exist", args.extractPath)

            return@ParboiledSoldier FAILURE
        }

        if (args.destDir == null) {
            printlnErrLocale("commands.extract.err_no_dest_dir")

            return@ParboiledSoldier FAILURE
        }

        if (!args.destDir.exists() && !args.destDir.mkdirs()) {
            printlnErrLocale("errors.files.cant_create_dir", args.destDir)

            return@ParboiledSoldier FAILURE
        }

        val (dataSource, compression) = decompress(args.extractPath::inputStream)

        val result = GurrenMechanic.EXTRACTABLE_ARCHIVES.map { format -> format.read(source = dataSource) }
                .filter(FormatResult<*>::didSucceed)
                .sortedBy(FormatResult<*>::chance)
                .firstOrNull()
                ?.obj

        val files: Iterator<Pair<String, InputStream>>
        val totalCount: Int

        when (result) {
            null -> {
                printlnErrLocale("commands.extract.err_no_format_for", args.extractPath)

                return@ParboiledSoldier FAILURE
            }

            is AWB -> {
                files = result.entries.iterator { entry -> entry.id.toString() to entry.inputStream }
                totalCount = result.entries.count { entry -> entry.id.toString().matches(regex) }
            }

            is CPK -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }
            }

            is Pak -> {
                files = result.files.iterator { entry -> entry.index.toString() to entry.inputStream }
                totalCount = result.files.count { entry -> entry.index.toString().matches(regex) }
            }

            is SPC -> {
                files = if (args.leaveCompressed!!) {
                    result.files.iterator { entry -> entry.name to entry.rawInputStream }
                } else {
                    result.files.iterator { entry -> entry.name to entry.inputStream }
                }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }
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
                totalCount = entries.count { pair -> pair.first.matches(regex) }
            }

            is WAD -> {
                files = result.files.iterator { entry -> entry.name to entry.inputStream }
                totalCount = result.files.count { entry -> entry.name.matches(regex) }
            }

            is ZipFile -> {
                files = result.entries().iterator { entry -> entry.name to result.getInputStream(entry) }
                totalCount = result.entries().asSequence().count { entry -> entry.name.matches(regex) }
            }

            else -> {
                printlnErrLocale("commands.extract.err_unk_format", result)

                return@ParboiledSoldier FAILURE
            }
        }

        val per = (100.0 / totalCount.toDouble())
        var last: Double = 0.0

        if (compression.isEmpty())
            printlnLocale("commands.extract.archive_type", result::class.simpleName)
        else
            printlnLocale("commands.extract.compressed_archive_type", compression.joinToString(" > ") { format ->
                format::class.simpleName ?: format::class.jvmName
            }, result::class.simpleName)
        printlnLocale("commands.extract.extracting_files", totalCount, args.destDir)

        val printOut: (Double) -> Unit = if (cockpit.args.ansiEnabled) { percent ->
            print("\r${GurrenMechanic.PERCENT_FORMAT.format(percent)}%")
        } else { percent ->
            print("\r${GurrenMechanic.PERCENT_FORMAT.format(percent)}%")
        }

        files.forEachFiltered({ pair -> pair.first.matches(regex) }) { (fileName, raw) ->
            val file = File(args.destDir, fileName)
            file.parentFile.mkdirs()

            raw.use { stream -> FileOutputStream(file).use(stream::copyToStream) }
            last += per
            printOut(last)
        }

        println()
        printlnLocale("commands.extract.finished")

        return@ParboiledSoldier SUCCESS
    }
}