package info.spiralframework.console.jvm.commands.panels

import dev.brella.kornea.errors.common.Optional
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.toolkit.common.ProgressBar
import dev.brella.kornea.toolkit.coroutines.ascii.AsciiProgressBarConfig
import dev.brella.kornea.toolkit.coroutines.ascii.AsciiProgressBarStyle
import dev.brella.kornea.toolkit.coroutines.ascii.createArbitraryProgressBar
import dev.brella.kornea.toolkit.coroutines.ascii.createAsciiProgressBar
import dev.brella.kornea.toolkit.coroutines.ascii.progressBar
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.core.ReadableCompressionFormat
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.SpiralFormat
import info.spiralframework.formats.common.archives.SpiralArchive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

class GurrenExtractFilesPanel : ExtractFilesPanel, CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob()

    private var fileAnalysisProgressBar: Job? = null
    private var fileExtractionProgressBar: ProgressBar? = null
    private var fileExtractionCount: Int = 0

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

    @ExperimentalTime
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

    override suspend fun finishExtracting(context: SpiralContext, archive: SpiralArchive, destination: File) {
        fileExtractionProgressBar?.complete()
        fileExtractionProgressBar = null
        fileExtractionCount = 0
    }
}