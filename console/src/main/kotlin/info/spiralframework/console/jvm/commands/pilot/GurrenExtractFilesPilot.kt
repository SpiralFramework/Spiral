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
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.Optional
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.errors.common.filterNotNull
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrElseRun
import dev.brella.kornea.errors.common.switchIfEmpty
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.common.use
import dev.brella.kornea.toolkit.coroutines.ascii.AsciiProgressBarStyle
import dev.brella.kornea.toolkit.coroutines.ascii.progressBar
import info.spiralframework.base.binding.prompt
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cache
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.locale.promptExit
import info.spiralframework.base.common.logging.error
import info.spiralframework.base.common.logging.trace
import info.spiralframework.base.common.text.doublePadWindowsPaths
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.panels.ExtractFilesPanel
import info.spiralframework.console.jvm.commands.panels.GurrenExtractFilesPanel
import info.spiralframework.console.jvm.commands.panels.performFileAnalysis
import info.spiralframework.console.jvm.commands.pilot.GurrenExtractFilesPilot.extractFilesStub
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.data.extractFilesWithIOBuffer
import info.spiralframework.console.jvm.pipeline.DataSourceType
import info.spiralframework.console.jvm.pipeline.registerFunctionWithAliasesWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.registerFunctionWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.DefaultFormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.SpiralFormat
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.getSubfiles
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object GurrenExtractFilesPilot : CommandRegistrar {
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

                spiralContext.extractFilesStub(context, filePath, destination, filter, leaveCompressed, extractSubfiles, predictive, convert)
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

                spiralContext.extractFilesStub(knolusContext, filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
            }

            GurrenPilot.help("extract_files", "extract_files_wizard")
            GurrenPilot.help("extracting_files" to "extract_files", "file_extraction_wizard" to "extract_files_wizard")
        }
    }

    suspend fun SpiralContext.extractFilesStub(
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
                    subPath.inner.use { ds -> GurrenExtractFilesPanel().extractFiles(this, ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
                } else {
                    subPath.asString(knolusContext).doOnSuccess { subPath ->
                        extractFiles(subPath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
                    }
                }
            }
        } else if (filePath is DataSourceType) {
            filePath.inner.use { ds -> GurrenExtractFilesPanel().extractFiles(this, ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
        } else {
            filePath.asString(knolusContext).doOnSuccess { filePath ->
                extractFiles(filePath, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)
            }
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
            return AsyncFileDataSource(file).use { ds -> GurrenExtractFilesPanel().extractFiles(this, ds, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert) }
        } else {
            printlnLocale("commands.pilot.extract_files.err_path_not_file_or_directory", filePath)
            return
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun ExtractFilesPanel.extractFiles(context: SpiralContext, archiveDataSource: DataSource<*>, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean) {
        if (destDir == null) {
            noDestinationDirectory(context)
            return
        }

        val destination = File(destDir)

        if (destination.exists() && !destination.isDirectory) {
            destinationNotDirectory(context, destination)
            return
        }

        val (decompressedDataSource, archiveCompressionFormats) = if (archiveDataSource.reproducibility.isUnreliable() || archiveDataSource.reproducibility.isUnstable()) {
            archiveDataSource.cache(context).use { ds -> context.decompress(ds) }
        } else {
            context.decompress(archiveDataSource)
        }

        val readContext = DefaultFormatReadContext(decompressedDataSource.location?.replace("$(.+?)(?:\\+[0-9a-fA-F]+h|\\[[0-9a-fA-F]+h,\\s*[0-9a-fA-F]+h\\])".toRegex()) { result -> result.groupValues[1] }, GurrenPilot.game)
        val result = performFileAnalysis(context, GurrenShared.EXTRACTABLE_ARCHIVES) { formats ->
            formats.map { archive -> archive.identify(this, readContext, decompressedDataSource) }
                .also { results ->
                    trace {
                        trace("\rResults for \"{0}\":", archiveDataSource.location ?: constNull())
                        results.forEachIndexed { index, result ->
                            trace("\t{0}] == {1} ==", GurrenShared.EXTRACTABLE_ARCHIVES[index].name, result)
                        }
                    }
                }
                .filterIsInstance<FormatResult<Optional<SpiralArchive>, SpiralArchive>>()
                .sortedBy(FormatResult<*, *>::confidence)
                .asReversed()
                .firstOrNull()
        }

        if (result == null) {
            noFormatForFile(context, archiveDataSource)
            return
        }

        val archive: SpiralArchive = result.get()
            .filterNotNull()
            .getOrElseRun {
                result.format().read(context, readContext, decompressedDataSource)
                    .filterNotNull()
                    .getOrBreak {
                        noFormatForFile(context, archiveDataSource)
                        return
                    }
            }

        foundFileFormat(context, result, archiveCompressionFormats, archive)

        extractFilesFromArchive(context, archive, readContext.name, destination, filter.toRegex(), leaveCompressed, extractSubfiles, predictive, convert)

        println()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun ExtractFilesPanel.extractFilesFromArchive(
        context: SpiralContext,
        archive: SpiralArchive,
        archiveName: String? = null,
        destination: File,
        filter: Regex,
        leaveCompressed: Boolean,
        extractSubfiles: Boolean,
        predictive: Boolean,
        convert: Boolean
    ) {
        @Suppress("RedundantIf")
        //This causes an AssertionError if the if else statement is removed
//    val files = GurrenShared.getFilesForArchive(
//        this, archive, filter, if (leaveCompressed) true else false, if (predictive) true else false, GurrenPilot.game
//                                                                                                      ?: UnsafeDr1(), archiveName
//    )
        val files = archive.getSubfiles(context)

        if (archive.fileCount == 0) {
//            printLocale("commands.pilot.extract_files.empty_archive")
            archiveIsEmpty(context, archive)
            return
        }

        val time = measureTime {
//            progressBar(archive.fileCount.toLong(), loadingText = localise("commands.pilot.extract_files.extracting_files", archive.fileCount, destination), loadedText = null, trackStyle = AsciiProgressBarStyle.FLOWING) {
                extractFilesWithIOBuffer(context, archive, destination, filter, leaveCompressed, extractSubfiles, predictive, convert, files)
//            }
        }

        print('\n')
        context.printlnLocale("commands.pilot.extract_files.finished", time)
    }
}

suspend fun GurrenExtractFilesPilot.extractFilesFromArchive(
    context: SpiralContext,
    archive: SpiralArchive,
    archiveName: String? = null,
    destination: File,
    filter: Regex,
    leaveCompressed: Boolean,
    extractSubfiles: Boolean,
    predictive: Boolean,
    convert: Boolean
) = GurrenExtractFilesPanel().extractFilesFromArchive(context, archive, archiveName, destination, filter, leaveCompressed, extractSubfiles, predictive, convert)