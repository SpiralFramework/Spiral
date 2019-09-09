package info.spiralframework.console.commands.pilot

import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.text.ProgressTracker
import info.spiralframework.base.jvm.io.files.absoluteParentFile
import info.spiralframework.base.jvm.io.files.relativePathTo
import info.spiralframework.base.util.*
import info.spiralframework.console.CommandBuilders
import info.spiralframework.console.commands.data.ConvertArgs
import info.spiralframework.console.commands.data.ExtractArgs
import info.spiralframework.console.commands.shared.GurrenShared
import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.data.errors.ConvertResponse
import info.spiralframework.console.eventbus.CommandClass
import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.FAILURE
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.SUCCESS
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.*
import info.spiralframework.core.formats.FormatResult.Companion.NO_FORMAT_DEFINED
import info.spiralframework.formats.utils.dataContext
import info.spiralframework.formats.video.SFL
import org.parboiled.Action
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.reflect.jvm.jvmName

@Suppress("unused")
class GurrenPilot(override val parameterParser: ParameterParser) : CommandClass {
    companion object {
        /** Helper Variables */
        var keepLooping = AtomicBoolean(true)

        val PERCENT_FORMAT = DecimalFormat("00.00")
    }

    val builders = CommandBuilders(parameterParser)

    /** Rules */
    val extractRule = makeRuleWith(::ExtractArgs) { argsVar ->
        Sequence(
                Localised("commands.pilot.extract.extract"),
                Action<Any> { pushMarkerSuccessBase() },
                Optional(
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
                Optional(
                        InlineWhitespace(),
                        FilePath(),
                        Action<Any> { pushMarkerSuccessCommand() }
                )
        )
    }
    val convertRule = makeRuleWith(::ConvertArgs) { argsVar ->
        Sequence(
                Localised("commands.pilot.convert.convert"),
                Action<Any> { pushMarkerSuccessBase() },
                Optional(
                        InlineWhitespace(),
                        FirstOf(
                                Sequence(
                                        Localised("commands.pilot.convert.builder"),
                                        Action<Any> { argsVar.get().builder = true; true }
                                ),
                                Sequence(
                                        ExistingFilePath(),
                                        Action<Any> { argsVar.get().converting = pop() as? File; true }
                                )
                        ),
                        ZeroOrMore(
                                InlineWhitespace(),
                                FirstOf(
                                        Sequence(
                                                Localised("commands.pilot.convert.filter"),
                                                InlineWhitespace(),
                                                Filter(),
                                                Action<Any> { argsVar.get().filter = pop() as Regex; true }
                                        ),
                                        Sequence(
                                                Localised("commands.pilot.convert.from"),
                                                InlineWhitespace(),
                                                Parameter(),
                                                Action<Any> { argsVar.get().from = pop() as String; true }
                                        ),
                                        Sequence(
                                                Localised("commands.pilot.convert.to"),
                                                InlineWhitespace(),
                                                Parameter(),
                                                Action<Any> { argsVar.get().to = pop() as String; true }
                                        ),
                                        Sequence(
                                                Localised("commands.pilot.convert.builder"),
                                                Action<Any> { argsVar.get().builder = true; true }
                                        ),
                                        Sequence(
                                                Localised("commands.pilot.convert.game"),
                                                InlineWhitespace(),
                                                Parameter(),
                                                Action<Any> {
                                                    val name = pop() as String
                                                    val game = GurrenShared.DR_GAMES.firstOrNull { game -> game.names.any { str -> str.equals(name, true) } }
                                                    argsVar.get().game = game
                                                    game != null
                                                }
                                        )
                                )
                        ),
                        Action<Any> { pushMarkerSuccessCommand() }
                )
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
            printlnErrLocale("errors.files.doesnt_exist", file)

            return@ParboiledCommand FAILURE
        }

        //Next up, are we dealing with a singular file?
        if (file.isFile) {
            //If so, we can define a data source for it here
            //We decompress it in place, just in case it's compressed
            val (dataSource, compressionMethods) = decompress(file::inputStream)

            //We should now have a proper data source
            //We can now work on format identification
            val formatResult = GurrenShared.READABLE_FORMATS
                    .map { format ->
                        format.identify(this, FormatReadContext(file.name, null, file.absoluteParentFile?.dataContext), dataSource)
                    }
                    .filter(FormatResult<*>::didSucceed)
                    .sortedBy(FormatResult<*>::chance)
                    .asReversed()
                    .firstOrNull()

            if (formatResult != null) {
                //The file has an identifiable format.

                //Should result in something like DRVita > V3 > SPC >
                val compressionString = if (compressionMethods.isEmpty()) "" else compressionMethods.joinToString(" > ", postfix = " > ") { format -> format.name }

                //This concatenates them together, which will be something like DRVita > V3 > SPC > SRD, or just SRD if it's uncompressed
                val formatString = "${compressionString}${formatResult.nullableFormat?.name
                        ?: formatResult.obj.takeIf(Optional<*>::isPresent)?.get()?.javaClass?.name
                        ?: localise("commands.pilot.identify.unknown_format_name")}"

                //Print it all out
//                if (SpiralModel.tableOutput) {
//                    println(FlipTable.of(arrayOf("File", "Format"), arrayOf(arrayOf(file.absolutePath, formatString))))
//                } else {
                printlnLocale("commands.pilot.identify.identified", file.absolutePath)
                printlnLocale("commands.pilot.identify.identified_format", formatString)
//                }

                return@ParboiledCommand SUCCESS
            }
        } else if (file.isDirectory) {
            val subfiles = file.walk().filter { subfile -> !subfile.name.startsWith(".") && subfile.isFile }.toList()
            val dataContext = file.dataContext
            val resultList = ProgressTracker(downloadingText = "commands.pilot.identify.folder_progress", downloadedText = "commands.pilot.identify.folder_finished") {
                subfiles.mapIndexed { index, subfile ->
                    try {
                        //We decompress it in place, just in case it's compressed
                        val (dataSource, compressionMethods) = decompress(subfile::inputStream)

                        //We should now have a proper data source
                        //We can now work on format identification
                        val formatResult = GurrenShared.READABLE_FORMATS
                                .map { format -> format.identify(this@ParboiledCommand, FormatReadContext(subfile.name, null, dataContext), dataSource) }
                                .filter(FormatResult<*>::didSucceed)
                                .sortedBy(FormatResult<*>::chance)
                                .asReversed()
                                .firstOrNull()

                        if (formatResult != null) {
                            //The file has an identifiable format.

                            //Should result in something like DRVita > V3 > SPC >
                            val compressionString = if (compressionMethods.isEmpty()) "" else compressionMethods.joinToString(" > ", postfix = " > ")

                            //This concatenates them together, which will be something like DRVita > V3 > SPC > SRD, or just SRD if it's uncompressed
                            return@mapIndexed subfile to "${compressionString}${formatResult.nullableFormat?.name
                                    ?: formatResult.obj.takeIf(Optional<*>::isPresent)?.get()?.javaClass?.name
                                    ?: localise("commands.pilot.identify.unknown_format_name")}"
                        } else {
                            return@mapIndexed subfile to localise("commands.pilot.identify.no_format")
                        }
                    } finally {
                        trackDownload(index.toLong(), subfiles.size.toLong())
                    }
                }
            }

            printlnLocale("commands.pilot.identify.identified_header")
            println(resultList.joinToString("\n") { (subfile, format) -> localise("commands.pilot.identify.identified_many", subfile relativePathTo file, format) })

            return@ParboiledCommand SUCCESS
        }

        return@ParboiledCommand FAILURE
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

        val result = GurrenShared.EXTRACTABLE_ARCHIVES.map { format -> format.read(this, source = dataSource) }
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
            printlnLocale("commands.pilot.extract.archive_type", result::class.simpleName ?: result::class.jvmName)
        else
            printlnLocale("commands.pilot.extract.compressed_archive_type", compression.joinToString(" > ") { format ->
                format::class.simpleName ?: format::class.jvmName
            }, result::class.simpleName ?: result::class.jvmName)
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

    val convert = ParboiledCommand(convertRule) { stack ->
        val builderArgs = (stack[0] as ConvertArgs)//.makeImmutable(defaultFilter = ".*", defaultLeaveCompressed = false)
        if (builderArgs.builder || builderArgs.converting == null) {
            //Builder
            if (builderArgs.converting == null) {
                printLocale("commands.pilot.convert.builder.converting")
                builderArgs.converting = builders.filePath()
            }

            if (builderArgs.from == null) {
                printLocale("commands.pilot.convert.builder.to")
                builderArgs.from = builders.parameter()
            }

            if (builderArgs.from == null) {
                printLocale("commands.pilot.convert.builder.from")
                builderArgs.from = builders.parameter()
            }

            if (builderArgs.filter == null) {
                printLocale("commands.pilot.convert.builder.filter")
                builderArgs.filter = builders.filter() ?: Regex(".*")
            }
        }

        val args = builderArgs.makeImmutable(defaultFilter = Regex(".*"))

        if (args.converting == null) {
            printlnErrLocale("commands.pilot.convert.err_no_converting")

            return@ParboiledCommand FAILURE
        }

        if (!args.converting.exists()) {
            printlnErrLocale("errors.files.doesnt_exist", args.converting)

            return@ParboiledCommand FAILURE
        }

        if (args.converting.isFile) {
            val file = args.converting

            //If so, we can define a data source for it here
            //We decompress it in place, just in case it's compressed
            val (dataSource, compressionMethods) = decompress(file::inputStream)

            //We should now have a proper data source
            //We can now work on format identification
            val formatResult = if (args.from == null) {
                GurrenShared.READABLE_FORMATS
                        .map { format ->
                            format.identify(this, FormatReadContext(file.name, args.game, file.absoluteParentFile?.dataContext), dataSource)
                        }
                        .filter(FormatResult<*>::didSucceed)
                        .sortedBy(FormatResult<*>::chance)
                        .asReversed()
                        .firstOrNull()
            } else {
                val formatForName = GurrenShared.READABLE_FORMATS
                        .firstOrNull { format -> format.name.equals(args.from, true) }

                if (formatForName == null) {
                    printlnLocale("commands.pilot.convert.err_no_known_format_name", "reading", args.from)
                    return@ParboiledCommand FAILURE
                }

                formatForName.identify(this, FormatReadContext(file.name, args.game, file.absoluteParentFile?.dataContext), dataSource)
                        .takeIf(FormatResult<*>::didSucceed)
            }

            if (formatResult != null) {
                //The file has an identifiable format.
                //Let's read the data
                if (formatResult.format === NO_FORMAT_DEFINED) {
                    //No format included, we can't work with this
                    printlnErrLocale("commands.pilot.convert.err_no_format_in_result", formatResult.obj.takeIf(Optional<*>::isPresent)?.get()?.javaClass?.name
                            ?: localise("commands.pilot.convert.unknown_format_name"))
                    return@ParboiledCommand FAILURE
                }

                //Should result in something like DRVita > V3 > SPC >
                val compressionString = if (compressionMethods.isEmpty()) "" else compressionMethods.joinToString(" > ", postfix = " > ") { format -> format.name }
                val resultString = formatResult.format.name

                //This concatenates them together, which will be something like DRVita > V3 > SPC > SRD, or just SRD if it's uncompressed
                val formatString = "${compressionString}${resultString}"

                val ourFormat = formatResult.format as? ReadableSpiralFormat<*>
                if (ourFormat == null) {
                    //Our format isn't a readable format, wtf?
                    printlnErrLocale("commands.pilot.convert.err_unknown_read_format", resultString)
                    return@ParboiledCommand FAILURE
                }

                val formatConvertTo = if (args.to == null) {
                    val preferred = ourFormat.preferredConversionFormat()
                    if (preferred == null) {
                        //No preferred format
                        printlnErrLocale("commands.pilot.convert.err_no_preferred_write_format", ourFormat.name)
                        return@ParboiledCommand FAILURE
                    }

                    preferred
                } else {
                    val formatForName = GurrenShared.WRITABLE_FORMATS
                            .firstOrNull { format -> format.name.equals(args.to, true) }

                    if (formatForName == null) {
                        printlnErrLocale("commands.pilot.convert.err_no_known_format_name", "writing", args.to)
                        return@ParboiledCommand FAILURE
                    }

                    formatForName
                }

                val data = formatResult.obj.takeIf(Optional<*>::isPresent)?.get()
                        ?: ourFormat.read(this, FormatReadContext(file.name, args.game, file.absoluteParentFile?.dataContext), dataSource).safeObj

                if (data == null) {
                    printlnErrLocale("commands.pilot.convert.err_no_readable_data", ourFormat.name)
                    return@ParboiledCommand FAILURE
                }

                if (!formatConvertTo.supportsWriting(this, data)) {
                    printlnErrLocale("commands.pilot.convert.err_write_not_supported", formatConvertTo.name, data::class.java.name)
                    return@ParboiledCommand FAILURE
                }

                val output = File("${file.absolutePath.substringBeforeLast('.')}.${formatConvertTo.extension
                        ?: SpiralFormat.DEFAULT_EXTENSION}")
                val response = FileOutputStream(output).use { outStream ->
                    formatConvertTo.write(this, FormatWriteContext(output.name, args.game, file.absoluteParentFile?.dataContext), data, outStream)
                }

                when (response) {
                    FormatWriteResponse.SUCCESS -> {
                        printlnLocale("commands.pilot.convert.response", file, formatString, formatConvertTo.name)
                        return@ParboiledCommand SUCCESS
                    }
                    FormatWriteResponse.WRONG_FORMAT -> {
                        printlnErrLocale("commands.pilot.convert.err_write_not_supported_result", formatConvertTo.name, data::class.java.name)
                        return@ParboiledCommand FAILURE
                    }
                    is FormatWriteResponse.FAIL -> {
                        printlnErrLocale("commands.pilot.convert.err_conversion_error", formatConvertTo.name, data::class.java.name)
                        response.reason.printStackTrace()
                        return@ParboiledCommand FAILURE
                    }
                }
            } else {
                if (args.from == null) {
                    printlnErrLocale("commands.pilot.convert.err_unknown_file", file)
                } else {
                    printlnErrLocale("commands.pilot.convert.err_not_of_format", file, args.from)
                }

                return@ParboiledCommand FAILURE
            }

        } else if (args.converting.isDirectory) {
            val dir = args.converting
            val subfiles = dir.walk().filter { subfile -> !subfile.name.startsWith(".") && subfile.isFile }.toList()
            val dataContext = args.converting.dataContext
            val (resultList, numberOfNulls) = ProgressTracker(downloadingText = "commands.pilot.convert.folder_progress", downloadedText = "commands.pilot.convert.folder_finished") {
                subfiles.mapIndexed { index, file ->
                    try {
                        //If so, we can define a data source for it here
                        //We decompress it in place, just in case it's compressed
                        val (dataSource, compressionMethods) = decompress(file::inputStream)

                        //We should now have a proper data source
                        //We can now work on format identification
                        val formatResult = if (args.from == null) {
                            GurrenShared.READABLE_FORMATS
                                    .map { format ->
                                        format.identify(this@ParboiledCommand, FormatReadContext(file.name, args.game, file.absoluteParentFile?.dataContext), dataSource)
                                    }
                                    .filter(FormatResult<*>::didSucceed)
                                    .sortedBy(FormatResult<*>::chance)
                                    .asReversed()
                                    .firstOrNull()
                        } else {
                            val formatForName = GurrenShared.READABLE_FORMATS
                                    .firstOrNull { format -> format.name.equals(args.from, true) }
                                    ?: return@mapIndexed ConvertResponse(file, null, null, localise("commands.pilot.convert.err_no_known_format_name", "reading", args.from))

                            formatForName.identify(this@ParboiledCommand, FormatReadContext(file.name, null, file.absoluteParentFile?.dataContext), dataSource)
                                    .takeIf(FormatResult<*>::didSucceed)
                        }

                        if (formatResult != null) {
                            //The file has an identifiable format.
                            //Let's read the data
                            if (formatResult.format === NO_FORMAT_DEFINED) {
                                //No format included, we can't work with this
                                return@mapIndexed ConvertResponse(
                                        file, null, null, localise("commands.pilot.convert.err_no_format_in_result", formatResult.obj.takeIf(Optional<*>::isPresent)?.get()?.javaClass?.name
                                        ?: localise("commands.pilot.convert.unknown_format_name")))
                            }

                            //Should result in something like DRVita > V3 > SPC >
                            val compressionString = if (compressionMethods.isEmpty()) "" else compressionMethods.joinToString(" > ", postfix = " > ") { format -> format.name }
                            val resultString = formatResult.format.name

                            //This concatenates them together, which will be something like DRVita > V3 > SPC > SRD, or just SRD if it's uncompressed
                            val formatString = "${compressionString}${resultString}"

                            val ourFormat = formatResult.format as? ReadableSpiralFormat<*>
                                    ?: //Our format isn't a readable format, wtf?
                                    return@mapIndexed ConvertResponse(file, formatString, null, localise("commands.pilot.convert.err_unknown_read_format", resultString))

                            val formatConvertTo = if (args.to == null) {
                                val preferred = ourFormat.preferredConversionFormat()
                                        ?: //No preferred format
                                        return@mapIndexed ConvertResponse(file, formatString, null, localise("commands.pilot.convert.err_no_preferred_write_format", ourFormat.name))

                                preferred
                            } else {
                                val formatForName = GurrenShared.WRITABLE_FORMATS
                                        .firstOrNull { format -> format.name.equals(args.to, true) }
                                        ?: return@mapIndexed ConvertResponse(file, formatString, null, localise("commands.pilot.convert.err_no_known_format_name", "writing", args.to))

                                formatForName
                            }

                            val data = formatResult.obj.takeIf(Optional<*>::isPresent)?.get()
                                    ?: ourFormat.read(this@ParboiledCommand, FormatReadContext(file.name, args.game, file.absoluteParentFile?.dataContext), dataSource).safeObj
                                    ?: return@mapIndexed ConvertResponse(file, formatString, null, localise("commands.pilot.convert.err_no_readable_data", ourFormat.name))

                            if (!formatConvertTo.supportsWriting(this@ParboiledCommand, data)) {
                                return@mapIndexed ConvertResponse(file, formatString, null, localise("commands.pilot.convert.err_write_not_supported", formatConvertTo.name, data::class.java.name))
                            }

                            val output = File("${file.absolutePath.substringBeforeLast('.')}.${formatConvertTo.extension
                                    ?: SpiralFormat.DEFAULT_EXTENSION}")
                            val response = FileOutputStream(output).use { outStream ->
                                formatConvertTo.write(this@ParboiledCommand, FormatWriteContext(output.name, args.game, file.absoluteParentFile?.dataContext), data, outStream)
                            }

                            when (response) {
                                FormatWriteResponse.SUCCESS -> {
                                    ConvertResponse(file, formatString, formatConvertTo.name, null)
                                }
                                FormatWriteResponse.WRONG_FORMAT -> {
                                    ConvertResponse(file, formatString, null, localise("commands.pilot.convert.err_write_not_supported_result", formatConvertTo.name, data::class.java.name))
                                }
                                is FormatWriteResponse.FAIL -> {
                                    ConvertResponse(file, formatString, null, buildString {
                                        appendln(localise("commands.pilot.convert.err_conversion_error", formatConvertTo.name, data::class.java.name))
                                        appendln(response.reason.retrieveStackTrace())
                                    })
                                }
                            }
                        } else {
                            return@mapIndexed if (args.from == null) {
                                ConvertResponse(file, null, null, localise("commands.pilot.convert.err_unknown_file", file))
                            } else {
                                null //ConvertResponse(file, null, null, locale("commands.pilot.convert.err_not_of_format", file, args.from))
                            }
                        }
                    } finally {
                        trackDownload(index.toLong(), subfiles.size.toLong())
                    }
                }
            }.let { list -> list.filterNotNull() to list.count { it == null } }

            if (numberOfNulls > 0)
                printlnLocale("commands.pilot.convert.skipped", numberOfNulls)

            val results = resultList.groupBy { response -> response.error == null }
            results[false]?.let { errorResults ->
                printlnLocale("commands.pilot.convert.converted_header_errors")
                println(errorResults.joinToString("\n") { (file, source, _, error) ->
                    localise("commands.pilot.convert.converted_errors_many", file relativePathTo dir, source
                            ?: localise("gurren.pilot.not_applicable"), error ?: constNull())
                })
            }

            results[true]?.let { validResults ->
                printlnLocale("commands.pilot.convert.converted_header")
                println(validResults.joinToString("\n") { (file, source, result) ->
                    localise("commands.pilot.convert.converted_many", file relativePathTo file, source
                            ?: localise("gurren.pilot.not_applicable"), result
                            ?: localise("gurren.pilot.not_applicable"))
                })
            }

            return@ParboiledCommand SUCCESS
        }

        return@ParboiledCommand FAILURE
    }

    val debug = ParboiledCommand(makeRule { IgnoreCase("debug") }) { stack ->
        val sfl = SFL(this, File("/Users/undermybrella/Workspace/KSPIRAL/shinkiro/dr1_data/Dr1/data/all/flash/fla_735/0")::inputStream)!!
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