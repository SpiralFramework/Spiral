package info.spiralframework.console.jvm.data

import dev.brella.kornea.errors.common.Optional
import dev.brella.kornea.errors.common.getOrElseRun
import dev.brella.kornea.errors.common.getOrNull
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyTo
import dev.brella.kornea.io.common.useInputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import dev.brella.kornea.toolkit.common.ProgressBar
import dev.brella.kornea.toolkit.common.closeAfter
import dev.brella.kornea.toolkit.common.use
import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.logging.SpiralLogger.NoOp.debug
import info.spiralframework.base.common.logging.error
import info.spiralframework.console.jvm.commands.panels.ExtractFilesPanel
import info.spiralframework.console.jvm.commands.pilot.GurrenExtractFilesPilot
import info.spiralframework.console.jvm.commands.pilot.GurrenPilot
import info.spiralframework.console.jvm.commands.pilot.extractFilesFromArchive
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.core.formats.DefaultFormatReadContext
import info.spiralframework.core.formats.DefaultFormatWriteContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.FormatWriteResponse
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.SpiralArchiveSubfile
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

const val MAX_BUFFER_SIZE = 65_536
const val MAX_BUFFER_ALLOCATION = 16_000_000
const val MAX_BUFFER_OPERATIONS = MAX_BUFFER_ALLOCATION / MAX_BUFFER_SIZE

const val MAX_MISSING_DATA_COUNT = 8

val BUFFERED_IO_DISPATCHER = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

sealed class BufferIOOperation {
    class Open(val flow: OutputFlow) : BufferIOOperation()
    class Write(val buffer: ByteArray) : BufferIOOperation() {
        constructor(buffer: ByteArray, offset: Int, length: Int) : this(buffer.copyOfRange(offset, offset + length))
    }

    object Close : BufferIOOperation()
    class CloseAndPerform(val perform: suspend () -> Unit) : BufferIOOperation()
}

class IOBuffer {
    private var out: OutputFlow? = null
    private val outputBuffer: MutableList<BufferIOOperation> = ArrayList()

    @ExperimentalUnsignedTypes
    suspend fun buffer(bufferOP: BufferIOOperation) {
        if (outputBuffer.size >= MAX_BUFFER_OPERATIONS) {
            flush()
        }

        outputBuffer.add(bufferOP)
    }

    suspend fun flush() {
        outputBuffer.forEach { op ->
            when (op) {
                is BufferIOOperation.Open -> out = op.flow
                is BufferIOOperation.Write -> out?.write(op.buffer)
                is BufferIOOperation.Close -> {
                    out?.close()
                    out = null
                }
                is BufferIOOperation.CloseAndPerform -> {
                    out?.close()
                    out = null

                    op.perform()
                }
            }
        }

        outputBuffer.clear()
    }
}

@OptIn(ExperimentalTime::class)
suspend inline fun ExtractFilesPanel.extractFilesWithIOBuffer(
    context: SpiralContext,
    archive: SpiralArchive,
    destination: File,
    filter: Regex,
    leaveCompressed: Boolean,
    extractSubfiles: Boolean,
    predictive: Boolean,
    convert: Boolean,
    files: Flow<SpiralArchiveSubfile<*>>
) {
    withContext(BUFFERED_IO_DISPATCHER) {
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
                                context.error("{0} has no more remaining data, waited {1} times", flow.location ?: source.location ?: "(unknown)", missingDataCount)
                                subfileHasNoMoreData(context, archive, destination, name, flow, source, missingDataCount)
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

@ExperimentalUnsignedTypes
suspend inline fun ProgressBar.extractFilesSequential(
    context: SpiralContext,
    destination: File,
    filter: Regex,
    leaveCompressed: Boolean,
    extractSubfiles: Boolean,
    predictive: Boolean,
    convert: Boolean,
    files: Flow<Pair<String, DataSource<*>>>
) {
    withContext(BUFFERED_IO_DISPATCHER) {
        var copied = 0L

        files.onCompletion { complete() }
            .collect { (name, source) ->
                closeAfter(source) {
                    source.useInputFlow { flow ->
                        val outFile = File(destination, name)
                        outFile.parentFile.mkdirs()

                        AsyncFileOutputFlow(outFile).use { async -> closeAfter(flow) { flow.copyTo(async, bufferSize = minOf(1_000_000, source.dataSize?.toInt() ?: Int.MAX_VALUE)) } }

                        if (extractSubfiles) {
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
                        }

                        trackProgress(++copied)
                    }
                }
            }
    }
}