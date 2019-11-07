package info.spiralframework.console.commands.mechanic

import info.spiralframework.base.common.locale.printlnErrLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.text.ProgressTracker
import info.spiralframework.base.jvm.forEachFiltered
import info.spiralframework.base.util.copyToStream
import info.spiralframework.console.commands.data.CompileArgs
import info.spiralframework.console.commands.data.ExtractArgs
import info.spiralframework.console.commands.shared.GurrenShared
import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.eventbus.CommandClass
import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.FAILURE
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.SUCCESS
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.SpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.core.formats.archives.*
import info.spiralframework.osl.parserAction
import org.parboiled.Action
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import kotlin.reflect.jvm.jvmName

@Suppress("unused")
class GurrenMechanic(override val parameterParser: ParameterParser) : CommandClass {
    companion object {
        val COMPILABLE_ARCHIVES = arrayOf<WritableSpiralFormat>(
                CpkFormat, PakFormat, SpcFormat,
                WadFormat, ZipFormat
        )

        val PERCENT_FORMAT = DecimalFormat("00.00")
    }

    /** Rules */
    val separatorRule = makeRule { FirstOf(InlineWhitespace(), '=') }
    val extractRule = makeRuleWith(::ExtractArgs) { argsVar ->
        Sequence(
                Localised("commands.mechanic.extract.extract"),
                Action<Any> { pushMarkerSuccessBase() },
                Optional(
                        ParamSeparator(),
                        ExistingMechanicFilePath(),
                        Action<Any> { argsVar.get().extractPath = pop() as? File; true },
                        ZeroOrMore(
                                ParamSeparator(),
                                FirstOf(
                                        Sequence(
                                                Localised("commands.mechanic.extract.filter"),
                                                ParamSeparator(),
                                                MechanicFilter(),
                                                Action<Any> { argsVar.get().filter = pop() as? Regex; true }
                                        ),
                                        Sequence(
                                                Localised("commands.mechanic.extract.dest_dir"),
                                                ParamSeparator(),
                                                MechanicFilePath(),
                                                Action<Any> { argsVar.get().destDir = pop() as? File; true }
                                        ),
                                        Sequence(
                                                Localised("commands.mechanic.extract.leave_compressed"),
                                                Action<Any> { argsVar.get().leaveCompressed = true; true }
                                        )
                                )
                        ),
                        Action<Any> { pushMarkerSuccessCommand() }
                )
        )
    }
    val compileRule = makeRuleWith(::CompileArgs) { argsVar ->
        Sequence(
                Localised("commands.mechanic.compile.compile"),
                Action<Any> { pushMarkerSuccessBase() },
                Optional(
                        ParamSeparator(),
                        MechanicFilePath(),
                        Action<Any> { argsVar.get().compilingDir = pop() as? File; true },
                        ZeroOrMore(
                                ParamSeparator(),
                                FirstOf(
                                        Sequence(
                                                Localised("commands.mechanic.compile.destination"),
                                                FirstOf(
                                                        Sequence(
                                                                Localised("commands.mechanic.compile.destination_empty"),
                                                                ParamSeparator(),
                                                                Action<Any> { argsVar.get().compileDestination = CompileArgs.EMPTY; true }
                                                        ),
                                                        Sequence(
                                                                MechanicFilePath(),
                                                                ParamSeparator(),
                                                                Action<Any> { argsVar.get().compileDestination = pop() as File; true }
                                                        )
                                                )
                                        ),
                                        Sequence(
                                                Localised("commands.mechanic.compile.format"),
                                                FirstOf(COMPILABLE_ARCHIVES.map(SpiralFormat::name).toTypedArray()),
                                                ParamSeparator(),
                                                Action<Any> { argsVar.get().formatOverride = COMPILABLE_ARCHIVES.first { format -> format.name.equals(pop() as String, true) }; true }
                                        ),
                                        Sequence(
                                                Localised("commands.mechanic.compile.filter"),
                                                ""
                                        )
                                )
                        ),
                        parserAction { pushMarkerSuccessCommand() }
                )
        )
    }

    val environmentRule = makeRule {
        Sequence(
                Localised("commands.mechanic.environment"),
                Action<Any> {
                    pushMarkerSuccessBase()
                    pushMarkerSuccessCommand()
                }
        )
    }

    /** Commands */

    val extract = ParboiledCommand(extractRule) { stack ->
        val args = (stack[0] as ExtractArgs).makeImmutable(defaultFilter = Regex(".*"), defaultLeaveCompressed = false)
        val regex = args.filter!!

        if (args.extractPath == null) {
            printlnErrLocale("commands.mechanic.extract.err_no_extract")

            return@ParboiledCommand FAILURE
        }

        if (!args.extractPath.exists()) {
            printlnErrLocale("errors.files.doesnt_exist", args.extractPath)

            return@ParboiledCommand FAILURE
        }

        if (args.destDir == null) {
            printlnErrLocale("commands.mechanic.extract.err_no_dest_dir")

            return@ParboiledCommand FAILURE
        }

        if (!args.destDir.exists() && !args.destDir.mkdirs()) {
            printlnErrLocale("errors.files.cant_create_dir", args.destDir)

            return@ParboiledCommand FAILURE
        }

        val (dataSource, compression) = decompress(args.extractPath::inputStream)

        val result = GurrenShared.EXTRACTABLE_ARCHIVES.map { format -> format.read(context = this, source = dataSource) }
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

        val fileResult = GurrenShared.extractGetFilesForResult(args, result, regex)

        if (fileResult == null) {
            printlnErrLocale("commands.mechanic.extract.err_unk_format", result)

            return@ParboiledCommand FAILURE
        }

        files = fileResult.first
        totalCount = fileResult.second

        if (compression.isEmpty())
            printlnLocale("commands.mechanic.extract.archive_type", result::class.simpleName ?: result::class.jvmName)
        else
            printlnLocale("commands.mechanic.extract.compressed_archive_type", compression.joinToString(" > ") { format ->
                format::class.simpleName ?: format::class.jvmName
            }, result::class.simpleName ?: result::class.jvmName)
        printlnLocale("commands.mechanic.extract.extracting_files", totalCount, args.destDir)

        var extracted: Long = 0
        ProgressTracker(downloadingText = "commands.extract.extracting_progress", downloadedText = "commands.extract.finished") {
            trackDownload(0, totalCount)
            files.forEachFiltered({ pair -> pair.first.matches(regex) }) { (fileName, raw) ->
                val file = File(args.destDir, fileName)
                file.parentFile.mkdirs()

                raw.use { stream -> FileOutputStream(file).use(stream::copyToStream) }
                trackDownload(++extracted, totalCount)
            }
        }

        println()
        printlnLocale("commands.mechanic.extract.finished")

        return@ParboiledCommand SUCCESS
    }
    val compile = ParboiledCommand(compileRule) { stack ->
        println("We're in boyo")
        return@ParboiledCommand SUCCESS
    }
    val environment = ParboiledCommand(environmentRule) {
        println(retrieveEnvironment().entries.joinToString("\n") { (k, v) -> "$k: $v"})
        return@ParboiledCommand SUCCESS
    }
}