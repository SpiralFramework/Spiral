package info.spiralframework.core.panels

import dev.brella.kornea.base.common.*
import dev.brella.kornea.errors.common.filterNotNull
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.useInputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cache
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.logging.error
import info.spiralframework.base.common.logging.trace
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.*
import info.spiralframework.core.common.BufferIOOperation
import info.spiralframework.core.common.IOBuffer
import info.spiralframework.core.common.MAX_BUFFER_SIZE
import info.spiralframework.core.common.MAX_MISSING_DATA_COUNT
import info.spiralframework.core.common.formats.GenericFormatSuccess
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.getSubfiles
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import java.io.File
import kotlin.math.pow
import kotlin.math.roundToLong

public interface ExtractFilesCommand {
    public val archiveFormats: List<ReadableSpiralFormat<SpiralArchive>>

    public suspend fun noDestinationDirectory(context: SpiralContext)
    public suspend fun destinationNotDirectory(context: SpiralContext, destination: File)

    public suspend fun beginFileAnalysis(context: SpiralContext, formats: List<ReadableSpiralFormat<SpiralArchive>>)

    public suspend fun noFormatForFile(context: SpiralContext, dataSource: DataSource<*>)
    public suspend fun foundFileFormat(
        context: SpiralContext,
        result: GenericFormatSuccess<Optional<SpiralArchive>>,
        compressionFormats: List<ReadableCompressionFormat>?,
        archive: SpiralArchive
    )

    public suspend fun finishFileAnalysis(context: SpiralContext)

    public suspend fun archiveIsEmpty(context: SpiralContext, archive: SpiralArchive)

    public suspend fun beginExtracting(context: SpiralContext, archive: SpiralArchive, destination: File)

    public suspend fun beginExtractingSubfile(
        context: SpiralContext,
        archive: SpiralArchive,
        destination: File,
        subfile: String,
        flow: InputFlow,
        source: DataSource<*>
    )

    public suspend fun subfileIsEmpty(
        context: SpiralContext,
        archive: SpiralArchive,
        destination: File,
        subfile: String,
        flow: InputFlow,
        source: DataSource<*>
    )

    public suspend fun subfileHasNoMoreData(
        context: SpiralContext,
        archive: SpiralArchive,
        destination: File,
        subfile: String,
        flow: InputFlow,
        source: DataSource<*>,
        waitCount: Int
    )

    public suspend fun finishExtractingSubfile(
        context: SpiralContext,
        archive: SpiralArchive,
        destination: File,
        subfile: String,
        flow: InputFlow,
        source: DataSource<*>
    )

    public suspend fun finishExtracting(context: SpiralContext, archive: SpiralArchive, destination: File)

    public suspend operator fun invoke(
        context: SpiralContext,
        readContext: SpiralProperties,
        archiveDataSource: DataSource<*>,
        destDir: String?,
        filter: String,
        leaveCompressed: Boolean,
        extractSubfiles: Boolean,
        predictive: Boolean,
        convert: Boolean
    ) {
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

        val result = performFileAnalysis(context, archiveFormats.sortedAgainst(readContext)) { formats ->
            formats.mapFormatResults { archive -> archive.identify(this, readContext, decompressedDataSource) }
                .also { results ->
                    trace {
                        trace("\rResults for \"{0}\":", archiveDataSource.location ?: constNull())
                        results.forEachIndexed { index, result ->
                            trace("\t{0}] == {1} ==", archiveFormats[index].name, result)
                        }
                    }
                }
                .filterSuccesses()
                .sortedByConfidence()
                .asReversed()
                .firstOrNull()
        }

        if (result == null) {
            noFormatForFile(context, archiveDataSource)
            return
        }

        val archive: SpiralArchive = result.value
            .filterNotNull()
            .getOrElseRun {
                result.format.read(context, readContext, decompressedDataSource)
                    .filterNotNull()
                    .getOrBreak {
                        noFormatForFile(context, archiveDataSource)
                        return
                    }.value
            }

        foundFileFormat(context, result, archiveCompressionFormats, archive)

        this(
            context,
            archive,
            readContext[ISpiralProperty.FileName],
            destination,
            filter.toRegex(),
            leaveCompressed,
            extractSubfiles,
            predictive,
            convert
        )
    }

    public suspend operator fun invoke(
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
        val files = archive.getSubfiles(context)

        if (archive.fileCount == 0) {
            archiveIsEmpty(context, archive)
            return
        }

        val output = IOBuffer()

        beginExtracting(context, archive, destination)

        files.onCompletion {
            output.flush()

            finishExtracting(context, archive, destination)
        }.catch { th ->
            th.printStackTrace()
        }.collect { (name, source) ->
            closeAfter(source) {
                source.useInputFlow { flow ->
                    val outFile = File(destination, name)
                    outFile.parentFile.mkdirs()

                    val out = AsyncFileOutputFlow(outFile)

                    output.buffer(BufferIOOperation.Open(out))

                    beginExtractingSubfile(context, archive, destination, name, flow, source)

                    var bytesCopied: Long = 0
                    val buffer = ByteArray(minOf(MAX_BUFFER_SIZE, source.dataSize?.toInt() ?: Int.MAX_VALUE))
                    if (buffer.isEmpty()) {
                        context.error("{0} is empty, cannot extract", flow.location ?: source.location ?: "(unknown)")
                        subfileIsEmpty(context, archive, destination, name, flow, source)
                        output.buffer(BufferIOOperation.Close)
                        return@closeAfter
                    }

                    var bytes = flow.read(buffer)
                    var missingDataCount = 0
                    while (bytes != null) {
                        if (bytes == 0) {
                            if (missingDataCount++ > MAX_MISSING_DATA_COUNT) {
                                context.error(
                                    "{0} has no more remaining data, waited {1} times",
                                    flow.location ?: source.location ?: "(unknown)",
                                    missingDataCount
                                )
                                subfileHasNoMoreData(
                                    context,
                                    archive,
                                    destination,
                                    name,
                                    flow,
                                    source,
                                    missingDataCount
                                )
                                output.buffer(BufferIOOperation.Close)
                                return@closeAfter
                            }

                            delay(2.0.pow(missingDataCount.toDouble()).roundToLong() * 250)
                        } else {
                            output.buffer(BufferIOOperation.Write(buffer, 0, bytes))
                            bytesCopied += bytes
                        }

                        bytes = flow.read(buffer)
                    }

                    output.buffer(BufferIOOperation.CloseAndPerform {
/*                        if (extractSubfiles) {
                            val didSubOutput = AsyncFileDataSource(outFile).use subUse@{ subfileDataSource ->
                                val readContext = DefaultFormatReadContext(name, GurrenPilot.game)
                                val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_sub_archive", loadedText = null) {
                                    GurrenShared.EXTRACTABLE_ARCHIVES.sortedBy { format -> (format.extension ?: "").compareTo(name.substringAfter('.')) }
                                        .map { archiveFormat -> archiveFormat.identify(context, readContext, subfileDataSource) }
                                        .filterIsInstance<FormatResult<Optional<SpiralArchive>, SpiralArchive>>()
                                        .sortedBy(FormatResult<*, *>::confidence)
                                        .asReversed()
                                        .firstOrNull()
                                }

                                if (result != null) {
                                    val subArchive: SpiralArchive = result.get().getOrElseRun {
                                        @Suppress("UNCHECKED_CAST")
                                        result.format().read(context, readContext, subfileDataSource).getOrNull()
                                    } ?: return@subUse false

                                    val subOutput = File(destination, name.substringBeforeLast('.'))
                                    GurrenExtractFilesPilot.extractFilesFromArchive(context, subArchive, name, subOutput, filter, leaveCompressed, extractSubfiles, predictive, convert)

                                    return@subUse true
                                }

                                false
                            }

                            if (didSubOutput) outFile.delete()
                        }

                        if (convert && outFile.exists()) {
                            val didSubOutput = AsyncFileDataSource(outFile).use subUse@{ subfileDataSource ->
                                val readContext = DefaultFormatReadContext(name, GurrenPilot.game)
                                val result = arbitraryProgressBar(loadingText = "commands.pilot.extract_files.analysing_sub_file", loadedText = null) {
                                    GurrenShared.CONVERTING_FORMATS.keys.sortedBy { format -> (format.extension ?: "").compareTo(name.substringAfter('.')) }
                                        .map { archiveFormat -> archiveFormat.identify(context, readContext, subfileDataSource) }
                                        .filterIsInstance<FormatResult<Optional<*>, *>>()
                                        .filter { result -> result.confidence() >= 0.90 }
                                        .sortedBy(FormatResult<*, *>::confidence)
                                        .asReversed()
                                        .firstOrNull()
                                }

                                if (result?.format() != null) {
                                    val readFormat = result.format()
                                    val subfile: Any = requireNotNull(result.get().getOrElseRun {
                                        readFormat.read(context, readContext, subfileDataSource).getOrNull()
                                    })

                                    val writeContext = DefaultFormatWriteContext(name, GurrenPilot.game)
                                    val writeFormat = GurrenShared.CONVERTING_FORMATS.getValue(readFormat)

                                    if (writeFormat.supportsWriting(context, writeContext, subfile)) {
                                        val existingExtension = name.substringAfterLast('.')
                                        val newOutput = File(destination,
                                                             if (existingExtension.equals(readFormat.extension, true) || existingExtension == "dat")
                                                                 name.replaceAfterLast(
                                                                     '.', writeFormat.extension
                                                                          ?: writeFormat.name
                                                                 )
                                                             else
                                                                 buildString {
                                                                     append(name)
                                                                     append('.')
                                                                     append(writeFormat.extension ?: writeFormat.name)
                                                                 }
                                        )

                                        val writeResult = AsyncFileOutputFlow(newOutput).use { out -> writeFormat.write(context, writeContext, subfile, out) }

                                        if (writeResult == FormatWriteResponse.SUCCESS) {
                                            return@subUse true
                                        } else {
                                            newOutput.delete()
                                            return@subUse false
                                        }
                                    } else {
                                        context.debug("Weird error; $writeFormat does not support writing $subfile")
                                    }
                                }

                                false
                            }

                            if (didSubOutput) outFile.delete()
                        }*/

                        finishExtractingSubfile(context, archive, destination, name, flow, source)
                    })
                }
            }
        }
    }
}

public suspend inline fun <T> ExtractFilesCommand.performFileAnalysis(
    context: SpiralContext,
    formats: List<ReadableSpiralFormat<SpiralArchive>>,
    operation: SpiralContext.(formats: List<ReadableSpiralFormat<SpiralArchive>>) -> T
): T {
    try {
        beginFileAnalysis(context, formats)
        return operation(context, formats)
    } finally {
        finishFileAnalysis(context)
    }
}