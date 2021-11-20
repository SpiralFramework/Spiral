package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.booleanTypeParameter
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.flagTypeParameter
import dev.brella.knolus.objectTypeParameter
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.types.KnolusArray
import dev.brella.knolus.types.KnolusString
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.knolus.types.asString
import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.use
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrElseRun
import dev.brella.kornea.errors.common.getOrNull
import dev.brella.kornea.errors.common.switchIfEmpty
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.Uri
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.common.ProgressBar
import dev.brella.kornea.toolkit.coroutines.ascii.AsciiProgressBarConfig
import dev.brella.kornea.toolkit.coroutines.ascii.AsciiProgressBarStyle
import dev.brella.kornea.toolkit.coroutines.ascii.createArbitraryProgressBar
import dev.brella.kornea.toolkit.coroutines.ascii.createAsciiProgressBar
import info.spiralframework.base.binding.prompt
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.locale.promptExit
import info.spiralframework.base.common.logging.error
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.pipeline.DataSourceType
import info.spiralframework.console.jvm.pipeline.registerFunctionWithAliasesWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.registerFunctionWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.ReadableCompressionFormat
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormat
import info.spiralframework.core.panels.ExtractFilesCommand
import info.spiralframework.formats.common.archives.SpiralArchive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class GurrenExtractFilesPilot(override val archiveFormats: List<ReadableSpiralFormat<SpiralArchive>>) : ExtractFilesCommand, CoroutineScope {
    companion object : CommandRegistrar {
        override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
            with(knolusContext) {
                registerFunctionWithContextWithoutReturn(
                    "extract_files",
                    objectTypeParameter("file_path").asOptional(),
                    stringTypeParameter("dest_dir").asOptional(),
                    stringTypeParameter("filter") withDefault ".+",
                    booleanTypeParameter("leave_compressed") withDefault false,
                    booleanTypeParameter("extract_subfiles") withDefault false,
                    booleanTypeParameter("predictive") withDefault false,
                    booleanTypeParameter("convert") withDefault false
                ) { context, filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert ->
                    val spiralContext = context.spiralContext().getOrBreak { return@registerFunctionWithContextWithoutReturn }

                    val filePath = filePath.getOrBreak { failure ->
                        spiralContext.printlnLocale("commands.pilot.extract_files.err_no_file")
                        if (failure is KorneaResult.WithException<*>)
                            spiralContext.error("commands.pilot.extract_files.err_no_file", failure.exception)
                        else
                            spiralContext.error("commands.pilot.extract_files.err_no_file", failure)

                        return@registerFunctionWithContextWithoutReturn
                    }

                    val destination: String = destDir.switchIfEmpty {
                        filePath.asString(context).flatMap { str ->
                            val file = File(str)
                            if (file.exists()) {
                                if (spiralContext.prompt("commands.pilot.extract_files.prompt_auto_dest")) {
                                    return@flatMap KorneaResult.success(file.absolutePath.substringBeforeLast('.'))
                                }
                            }

                            return@flatMap KorneaResult.empty<String>()
                        }
                    }.getOrBreak { failure ->
                        spiralContext.printlnLocale("commands.pilot.extract_files.err_no_dest_dir")
                        if (failure is KorneaResult.WithException<*>)
                            spiralContext.error("commands.pilot.extract_files.err_no_dest_dir", failure.exception)
                        else
                            spiralContext.error("commands.pilot.extract_files.err_no_dest_dir", failure)

                        return@registerFunctionWithContextWithoutReturn
                    }

                    extractFilesStub(spiralContext, context, filePath, destination, filter, leaveCompressed, extractSubfiles, predictive, convert)
                }

                registerFunctionWithAliasesWithContextWithoutReturn(
                    functionNames = arrayOf("extract_files_wizard", "extract_files_builder", "file_extract_wizard", "file_extraction_wizard"),
                    objectTypeParameter("file_path").asOptional(),
                    stringTypeParameter("dest_dir").asOptional(),
                    stringTypeParameter("filter") withDefault ".+",

                    flagTypeParameter("leave_compressed"),
                    flagTypeParameter("extract_subfiles"),
                    flagTypeParameter("predictive"),
                    flagTypeParameter("convert")
                ) { context, filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert ->
                    val filePath = filePath.getOrElseRun {
                        print(spiralContext.localise("commands.pilot.extract_files.builder.extract"))
                        spiralContext.constNull()
                        readLine()?.takeUnless(spiralContext.promptExit()::contains)?.doublePadWindowsPaths()?.trim('"')?.let { KnolusString(it) } ?: return@registerFunctionWithAliasesWithContextWithoutReturn
                    }

                    val destDir = destDir.getOrElseRun {
                        print(spiralContext.localise("commands.pilot.extract_files.builder.dest_dir"))
                        readLine()?.takeUnless(spiralContext.promptExit()::contains)?.doublePadWindowsPaths()?.trim('"') ?: return@registerFunctionWithAliasesWithContextWithoutReturn
                    }

                    extractFilesStub(spiralContext, knolusContext, filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
                }

                GurrenPilot.help("extract_files", "extract_files_wizard")
                GurrenPilot.help("extracting_files" to "extract_files", "file_extraction_wizard" to "extract_files_wizard")
            }
        }

        suspend fun extractFilesStub(
            spiralContext: SpiralContext,
            knolusContext: KnolusContext,
            filePath: KnolusTypedValue,
            destDir: String?,
            filter: String,
            leaveCompressed: Boolean,
            extractSubfiles: Boolean,
            predictive: Boolean,
            convert: Boolean
        ) {
            if (filePath is KnolusArray<*>) {
                filePath.array.forEach { subPath ->
                    if (subPath is DataSourceType) {
                        subPath.inner.use { ds -> GurrenExtractFilesPilot(spiralContext, ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
                    } else {
                        subPath.asString(knolusContext).doOnSuccess { subPath ->
                            extractFiles(spiralContext, subPath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
                        }
                    }
                }
            } else if (filePath is DataSourceType) {
                filePath.inner.use { ds -> GurrenExtractFilesPilot(spiralContext, ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
            } else {
                filePath.asString(knolusContext).doOnSuccess { filePath ->
                    extractFiles(spiralContext, filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
                }
            }
        }

        suspend fun extractFiles(spiralContext: SpiralContext, filePath: String?, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
            if (filePath == null) {
                spiralContext.printlnLocale("commands.pilot.extract_files.err_no_file")
                return
            }

            val file = File(filePath)

            if (!file.exists()) {
                spiralContext.printlnLocale("error.file.does_not_exist", filePath)
                return
            }

            if (file.isDirectory) {
                // Directory was passed; this is a potential ambiguity, so don't do anything here
                spiralContext.printlnLocale("commands.pilot.extract_files.err_path_is_directory", filePath)
                return
            } else if (file.isFile) {
                return AsyncFileDataSource(file).use { ds -> GurrenExtractFilesPilot(spiralContext, ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
            } else {
                spiralContext.printlnLocale("commands.pilot.extract_files.err_path_not_file_or_directory", filePath)
                return
            }
        }

        suspend operator fun invoke(
            context: SpiralContext,
            archiveDataSource: DataSource<*>,
            destDir: String?,
            filter: String,
            leaveCompressed: Boolean,
            extractSubfiles: Boolean,
            predictive: Boolean,
            convert: Boolean,
            readContext: SpiralProperties = GurrenPilot.formatContext.withOptional(ISpiralProperty.FileName, archiveDataSource.locationAsUri().getOrNull()?.takeIf { uri -> uri.protocol == Uri.PROTOCOL_FILE }?.path)
        ) = GurrenExtractFilesPilot(GurrenShared.EXTRACTABLE_ARCHIVES)(context, readContext, archiveDataSource, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)

        suspend operator fun invoke(
            context: SpiralContext,
            archive: SpiralArchive,
            archiveName: String? = null,
            destination: File,
            filter: Regex,
            leaveCompressed: Boolean,
            extractSubfiles: Boolean,
            predictive: Boolean,
            convert: Boolean
        ) = GurrenExtractFilesPilot(GurrenShared.EXTRACTABLE_ARCHIVES)(context, archive, archiveName, destination, filter, leaveCompressed, extractSubfiles, predictive, convert)
    }

    override val coroutineContext: CoroutineContext = SupervisorJob()

    private var fileAnalysisProgressBar: Job? = null
    private var fileExtractionProgressBar: ProgressBar? = null
    private var fileExtractionCount: Int = 0

    @ExperimentalTime
    private var extractionStarted: TimeMark? = null

    override suspend fun noDestinationDirectory(context: SpiralContext) {
        context.printlnLocale("commands.pilot.extract_files.err_no_dest_dir")
    }

    override suspend fun destinationNotDirectory(context: SpiralContext, destination: File) {
        context.printlnLocale("error.file.not_dir", destination)
    }

    override suspend fun beginFileAnalysis(context: SpiralContext, formats: List<ReadableSpiralFormat<SpiralArchive>>) {
        fileAnalysisProgressBar = createArbitraryProgressBar(loadingText = context.localise("commands.pilot.extract_files.analysing_archive"), loadedText = "")
    }

    override suspend fun noFormatForFile(context: SpiralContext, dataSource: DataSource<*>) {
        context.printlnLocale("commands.pilot.extract_files.err_no_format_for", dataSource.location ?: context.constNull())
    }

    override suspend fun foundFileFormat(context: SpiralContext, result: FormatResult<Optional<SpiralArchive>, SpiralArchive>, compressionFormats: List<ReadableCompressionFormat>?, archive: SpiralArchive) {
        if (compressionFormats?.isNotEmpty() != true) {
            context.printlnLocale("commands.pilot.extract_files.archive_type", result.format().name)
        } else {
            context.printlnLocale("commands.pilot.extract_files.compressed_archive_type", compressionFormats.joinToString(" > ", transform = SpiralFormat::name), result.format().name)
        }
    }

    override suspend fun finishFileAnalysis(context: SpiralContext) {
        fileAnalysisProgressBar?.cancelAndJoin()
        fileAnalysisProgressBar = null
    }

    override suspend fun archiveIsEmpty(context: SpiralContext, archive: SpiralArchive) {
        context.printlnLocale("commands.pilot.extract_files.empty_archive", archive)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun beginExtracting(context: SpiralContext, archive: SpiralArchive, destination: File) {
        fileExtractionProgressBar = createAsciiProgressBar(
            archive.fileCount.toLong(),
            loadingText = context.localise("commands.pilot.extract_files.extracting_files", archive.fileCount, destination),
            loadedText = null,
            trackStyle = AsciiProgressBarStyle.FLOWING,
            trackLength = AsciiProgressBarConfig.GLOBAL_DEFAULT_TRACK_LENGTH,
            showPercentage = AsciiProgressBarConfig.GLOBAL_DEFAULT_SHOW_PERCENTAGE,
            context = AsciiProgressBarConfig.defaultContext,
            output = AsciiProgressBarConfig.defaultOutput,
            updateInterval = AsciiProgressBarConfig.defaultUpdateInterval,
            updateOnEmpty = AsciiProgressBarConfig.GLOBAL_DEFAULT_UPDATE_ON_EMPTY,
        )

        fileExtractionCount = 0
        extractionStarted = TimeSource.Monotonic.markNow()
    }

    override suspend fun beginExtractingSubfile(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>) {
//        fileExtractionProgressBar?.
    }

    override suspend fun subfileIsEmpty(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>) {
        TODO("Not yet implemented")
    }

    override suspend fun subfileHasNoMoreData(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>, waitCount: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun finishExtractingSubfile(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>) {
        fileExtractionProgressBar?.trackProgress(++fileExtractionCount)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun finishExtracting(context: SpiralContext, archive: SpiralArchive, destination: File) {
        fileExtractionProgressBar?.complete()
        fileExtractionProgressBar = null
        fileExtractionCount = 0

        context.printlnLocale("commands.pilot.extract_files.finished", extractionStarted?.elapsedNow().toString())
    }
}