package info.spiralframework.console.jvm.commands.pilot

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cache
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.text.ProgressTracker
import info.spiralframework.base.common.text.arbitraryProgressBar
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.base.common.text.resetLine
import info.spiralframework.base.jvm.outOrElseGet
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.pipeline.PipelineContext
import info.spiralframework.console.jvm.pipeline.PipelineUnion
import info.spiralframework.console.jvm.pipeline.asFlattenedStringIfPresent
import info.spiralframework.console.jvm.pipeline.flattenIfPresent
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.*
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.games.UnsafeDr1
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.copyToOutputFlow
import org.abimon.kornea.io.common.use
import org.abimon.kornea.io.jvm.files.FileDataSource
import org.abimon.kornea.io.jvm.files.FileOutputFlow
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
object GurrenPilot : CommandRegistrar {
    /** Helper Variables */
    var keepLooping = AtomicBoolean(true)
    var game: DrGame? = null

    val PERCENT_FORMAT = DecimalFormat("00.00")

    val helpCommands: MutableSet<String> = HashSet()

    suspend fun SpiralContext.extractFilesStub(pipelineContext: PipelineContext, filePath: PipelineUnion.VariableValue?, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        if (filePath is PipelineUnion.VariableValue.ArrayType<*>) {
            filePath.array.forEach { subPath ->
                if (subPath is PipelineUnion.VariableValue.DataSourceType<*>) {
                    subPath.dataSource.use { ds -> extractFiles(ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
                } else {
                    extractFiles(subPath.asString(this, pipelineContext), destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
                }
            }
        } else if (filePath is PipelineUnion.VariableValue.DataSourceType<*>) {
            filePath.dataSource.use { ds -> extractFiles(ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
        } else {
            extractFiles(filePath?.asString(this, pipelineContext), destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
        }
    }

    suspend fun SpiralContext.extractFiles(filePath: String?, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        if (filePath == null) {
            printlnLocale("commands.pilot.extract_files.err_no_file")
            return
        }

        val file = File(filePath)

        if (!file.exists()) {
            printlnLocale("error.file.does_not_exist", filePath)
            return
        }

        if (file.isDirectory) {
            // Directory was passed; this is a potential ambiguity, so don't do anything here
            printlnLocale("commands.pilot.extract_files.err_path_is_directory", filePath)
            return
        } else if (file.isFile) {
            return FileDataSource(file).use { ds -> extractFiles(ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
        } else {
            printlnLocale("commands.pilot.extract_files.err_path_not_file_or_directory", filePath)
            return
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun SpiralContext.extractFiles(archiveDataSource: DataSource<*>, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        if (destDir == null) {
            printlnLocale("commands.pilot.extract_files.err_no_dest_dir")
            return
        }

        val destination = File(destDir)

        if (destination.exists() && !destination.isDirectory) {
            printlnLocale("error.file.not_dir", destination)
            return
        }

        val (decompressedDataSource, archiveCompressionFormats) = if (archiveDataSource.reproducibility.isUnreliable() || archiveDataSource.reproducibility.isUnstable()) {
            archiveDataSource.cache(this).use { ds -> decompress(ds) }
        } else {
            decompress(archiveDataSource)
        }

        val readContext = DefaultFormatReadContext(decompressedDataSource.location?.replace("$(.+?)(?:\\+[0-9a-fA-F]+h|\\[[0-9a-fA-F]+h,\\s*[0-9a-fA-F]+h\\])".toRegex()) { result -> result.groupValues[1] }, game)
        val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_archive", loadedText = null) {
            GurrenShared.EXTRACTABLE_ARCHIVES.map { archive -> archive.identify(this, readContext, decompressedDataSource) }
                    .filter(FormatResult<*>::didSucceed)
                    .sortedBy(FormatResult<*>::chance)
                    .asReversed()
                    .firstOrNull()
        }

        if (result == null) {
            printlnLocale("commands.pilot.extract_files.err_no_format_for", archiveDataSource.location ?: constNull())
            return
        }

        val archive: Any = result.obj.outOrElseGet {
            @Suppress("UNCHECKED_CAST")
            (result.format as ReadableSpiralFormat<out Any>).read(this, readContext, decompressedDataSource).obj
        }

        if (archiveCompressionFormats.isEmpty()) {
            printLocale("commands.pilot.extract_files.archive_type", result.format.name)
        } else {
            printLocale("commands.pilot.extract_files.compressed_archive_type", archiveCompressionFormats.joinToString(" > ", transform = SpiralFormat::name), result.format.name)
        }

        extractFilesFromArchive(decompressedDataSource, archive, readContext.name, destination, filter.toRegex(), leaveCompressed, extractSubfiles, predictive, convert)

        println()
    }

    suspend fun SpiralContext.extractFilesFromArchive(archiveDataSource: DataSource<*>, archive: Any, archiveName: String? = null, destination: File, filter: Regex, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        @Suppress("RedundantIf")
        //This causes an AssertionError if the if else statement is removed
        val files = GurrenShared.getFilesForArchive(this, archive, filter, if (leaveCompressed) true else false, if (predictive) true else false, game
                ?: UnsafeDr1(), archiveName)
        if (files == null) {
            resetLine()
            printLocale("commands.pilot.extract_files.empty_archive")
            return
        }

        val fileCount = files.count().toLong()
        try {
            val progressTracker = ProgressTracker(this, loadingText = localise("commands.pilot.extract_files.extracting_files", fileCount, destination), loadedText = "")
            var copied = 0L

            files.onCompletion {
                progressTracker.finishedDownload()
                resetLine()
                printLocale("commands.pilot.extract_files.finished")
            }.collect { (name, source) ->
                use(source) {
                    val flow = source.openInputFlow()
                    if (flow != null) {
                        use(flow) {
                            val output = File(destination, name)
                            output.parentFile.mkdirs()

                            FileOutputFlow(output).use(flow::copyToOutputFlow)

                            if (extractSubfiles) {
                                val didSubOutput = FileDataSource(output).use subUse@{ subfileDataSource ->
                                    val readContext = DefaultFormatReadContext(name, game)
                                    val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_sub_archive", loadedText = null) {
                                        GurrenShared.READABLE_FORMATS.sortedBy { format ->
                                                    (format.extension ?: "").compareTo(name.substringAfter('.'))
                                                }
                                                .map { archiveFormat -> archiveFormat.identify(this, readContext, subfileDataSource) }
                                                .filter(FormatResult<*>::didSucceed)
                                                .sortedBy(FormatResult<*>::chance)
                                                .asReversed()
                                                .firstOrNull()
                                    }

                                    if (result != null && result.format in GurrenShared.EXTRACTABLE_ARCHIVES) {
                                        val subArchive: Any = result.obj.outOrElseGet {
                                            @Suppress("UNCHECKED_CAST")
                                            (result.format as ReadableSpiralFormat<out Any>).read(this, readContext, subfileDataSource).obj
                                        }

                                        val subOutput = File(destination, name.substringBeforeLast('.'))
                                        extractFilesFromArchive(subfileDataSource, subArchive, name, subOutput, filter, leaveCompressed, extractSubfiles, predictive, convert)

                                        return@subUse true
                                    }

                                    false
                                }

                                if (didSubOutput) output.delete()
                            }

                            if (convert && output.exists()) {
                                val didSubOutput = FileDataSource(output).use subUse@{ subfileDataSource ->
                                    val readContext = DefaultFormatReadContext(name, game)
                                    val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_sub_file", loadedText = null) {
                                        GurrenShared.CONVERTING_FORMATS.keys.sortedBy { format ->
                                                    (format.extension ?: "").compareTo(name.substringAfter('.'))
                                                }
                                                .map { archiveFormat -> archiveFormat.identify(this, readContext, subfileDataSource) }
                                                .filter(FormatResult<*>::didSucceed)
                                                .filter { result -> result.chance >= 0.90 }
                                                .sortedBy(FormatResult<*>::chance)
                                                .asReversed()
                                                .firstOrNull()
                                    }

                                    if (result?.nullableFormat != null) {
                                        @Suppress("UNCHECKED_CAST")
                                        val readFormat = (result.format as ReadableSpiralFormat<out Any>)
                                        val subfile: Any = requireNotNull(result.obj.outOrElseGet {
                                            readFormat.read(this, readContext, subfileDataSource).obj
                                        })

                                        val writeContext = DefaultFormatWriteContext(name, game)
                                        val writeFormat = GurrenShared.CONVERTING_FORMATS.getValue(readFormat)

                                        if (writeFormat.supportsWriting(this, writeContext, subfile)) {
                                            val existingExtension = name.substringAfterLast('.')
                                            val newOutput = File(destination,
                                                    if (existingExtension.equals(readFormat.extension, true) || existingExtension == "dat")
                                                        name.replaceAfterLast('.', writeFormat.extension
                                                                ?: writeFormat.name)
                                                    else
                                                        buildString {
                                                            append(name)
                                                            append('.')
                                                            append(writeFormat.extension ?: writeFormat.name)
                                                        }
                                            )

                                            val writeResult = FileOutputFlow(newOutput).use { out -> writeFormat.write(this, writeContext, subfile, out) }

                                            if (writeResult == FormatWriteResponse.SUCCESS) {
                                                return@subUse true
                                            } else {
                                                newOutput.delete()
                                                return@subUse false
                                            }
                                        } else {
                                            debug("Weird error; $writeFormat does not support writing $subfile")
                                        }
                                    }

                                    false
                                }

                                if (didSubOutput) output.delete()
                            }
                        }
                    }
                }

                progressTracker.trackDownload(copied++, fileCount)
            }
        } finally {
            files.collect { (_, source) ->
                source.close()
            }
        }
    }

    override suspend fun register(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
        with(pipelineContext) {
            register("extract_files") {
                addParameter("file_path", PipelineUnion.VariableValue.NullType)
                addParameter("dest_dir", PipelineUnion.VariableValue.NullType)
                addParameter("filter", PipelineUnion.VariableValue.StringType(".+"))

                addFlag("leave_compressed")
                addFlag("extract_subfiles")
                addFlag("predictive")
                addFlag("convert")

                setFunction { spiralContext, pipelineContext, parameters ->
                    spiralContext.extractFilesStub(
                            pipelineContext,
                            parameters["FILEPATH"]?.flattenIfPresent(spiralContext, pipelineContext),
                            parameters["DESTDIR"]?.asFlattenedStringIfPresent(spiralContext, pipelineContext),
                            parameters["FILTER"]?.asString(spiralContext, pipelineContext) ?: ".+",
                            parameters["LEAVECOMPRESSED"]?.asBoolean(spiralContext, pipelineContext) ?: false,
                            parameters["EXTRACTSUBFILES"]?.asBoolean(spiralContext, pipelineContext) ?: false,
                            parameters["PREDICTIVE"]?.asBoolean(spiralContext, pipelineContext) ?: false,
                            parameters["CONVERT"]?.asBoolean(spiralContext, pipelineContext) ?: false
                    )

                    null
                }
            }

            register("extract_files_wizard") {
                addParameter("file_path", PipelineUnion.VariableValue.NullType)
                addParameter("dest_dir", PipelineUnion.VariableValue.NullType)
                addParameter("filter", PipelineUnion.VariableValue.StringType(".+"))

                addFlag("leave_compressed")
                addFlag("extract_subfiles")
                addFlag("predictive")
                addFlag("convert")

                setFunction { spiralContext, pipelineContext, parameters ->
                    var filePath = parameters["FILEPATH"]?.flattenIfPresent(spiralContext, pipelineContext)
                    var destDir = parameters["DESTDIR"]?.asFlattenedStringIfPresent(spiralContext, pipelineContext)
                    val filter = parameters["FILTER"]?.asString(spiralContext, pipelineContext) ?: ".+"
                    val leaveCompressed = parameters["LEAVECOMPRESSED"]?.asBoolean(spiralContext, pipelineContext)
                            ?: false
                    val extractSubfiles = parameters["EXTRACTSUBFILES"]?.asBoolean(spiralContext, pipelineContext)
                            ?: false
                    val predictive = parameters["PREDICTIVE"]?.asBoolean(spiralContext, pipelineContext) ?: false
                    val convert = parameters["CONVERT"]?.asBoolean(spiralContext, pipelineContext) ?: false

                    if (filePath == null) {
                        print(spiralContext.localise("commands.pilot.extract.builder.extract"))
                        filePath = readLine()?.doublePadWindowsPaths()?.trim('"')?.let(PipelineUnion.VariableValue::StringType)
                    }

                    if (destDir == null) {
                        print(spiralContext.localise("commands.pilot.extract.builder.dest_dir"))
                        destDir = readLine()?.doublePadWindowsPaths()?.trim('"')
                    }

                    spiralContext.extractFilesStub(pipelineContext, filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)

                    null
                }
            }

            register("show_environment") {
                setFunction { spiralContext, _, _ -> GurrenShared.showEnvironment(spiralContext); null }
            }
        }
    }
}