package info.spiralframework.console.jvm.commands.panels

import dev.brella.kornea.errors.common.Optional
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.ReadableCompressionFormat
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.formats.common.archives.SpiralArchive
import java.io.File

interface ExtractFilesPanel {
    suspend fun noDestinationDirectory(context: SpiralContext)
    suspend fun destinationNotDirectory(context: SpiralContext, destination: File)

    suspend fun beginFileAnalysis(context: SpiralContext, formats: List<ReadableSpiralFormat<SpiralArchive>>)

    suspend fun noFormatForFile(context: SpiralContext, dataSource: DataSource<*>)
    suspend fun foundFileFormat(context: SpiralContext, result: FormatResult<Optional<SpiralArchive>, SpiralArchive>, compressionFormats: List<ReadableCompressionFormat>?, archive: SpiralArchive)

    suspend fun finishFileAnalysis(context: SpiralContext)

    suspend fun archiveIsEmpty(context: SpiralContext, archive: SpiralArchive)

    suspend fun beginExtracting(context: SpiralContext, archive: SpiralArchive, destination: File)

    suspend fun beginExtractingSubfile(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>)

    suspend fun subfileIsEmpty(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>)
    suspend fun subfileHasNoMoreData(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>, waitCount: Int)

    suspend fun finishExtractingSubfile(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>)

    suspend fun finishExtracting(context: SpiralContext, archive: SpiralArchive, destination: File)
}

suspend inline fun <T> ExtractFilesPanel.performFileAnalysis(context: SpiralContext, formats: List<ReadableSpiralFormat<SpiralArchive>>, operation: SpiralContext.(formats: List<ReadableSpiralFormat<SpiralArchive>>) -> T): T {
    try {
        beginFileAnalysis(context, formats)
        return operation(context, formats)
    } finally {
        finishFileAnalysis(context)
    }
}