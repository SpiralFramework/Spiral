package info.spiralframework.gui.jvm

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.ReadableCompressionFormat
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.archives.AwbArchiveFormat
import info.spiralframework.core.common.formats.archives.CpkArchiveFormat
import info.spiralframework.core.common.formats.archives.PakArchiveFormat
import info.spiralframework.core.common.formats.archives.SpcArchiveFormat
import info.spiralframework.core.common.formats.archives.SrdArchiveFormat
import info.spiralframework.core.common.formats.archives.WadArchiveFormat
import info.spiralframework.core.common.formats.archives.ZipFormat
import info.spiralframework.core.panels.ExtractFilesCommand
import info.spiralframework.formats.common.archives.SpiralArchive
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt


class LagannExtractFilesCommand : ExtractFilesCommand, CoroutineScope {
    companion object {
//        suspend operator fun invoke(context: SpiralContext, archiveDataSource: DataSource<*>, destDir: String?, filter: String, leaveCompressed: Boolean, extractSubfiles: Boolean, predictive: Boolean, convert: Boolean, game: DrGame?) =
//            LagannExtractFilesCommand()(context, archiveDataSource.readContext(game), archiveDataSource, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)

        suspend operator fun invoke(
            context: SpiralContext,
            readContext: SpiralProperties,
            archiveDataSource: DataSource<*>,
            destDir: String?,
            filter: String,
            leaveCompressed: Boolean,
            extractSubfiles: Boolean,
            predictive: Boolean,
            convert: Boolean
        ) =
            LagannExtractFilesCommand()(context, readContext, archiveDataSource, destDir, filter, leaveCompressed, extractSubfiles, predictive, convert)

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
        ) = LagannExtractFilesCommand()(context, archive, archiveName, destination, filter, leaveCompressed, extractSubfiles, predictive, convert)
    }

    override val archiveFormats: List<ReadableSpiralFormat<SpiralArchive>> by lazy {
        mutableListOf(
            AwbArchiveFormat, CpkArchiveFormat, PakArchiveFormat,
//                SFLFormat,
            SpcArchiveFormat, SrdArchiveFormat, WadArchiveFormat,
            ZipFormat
        )
    }

    override val coroutineContext: CoroutineContext = SupervisorJob()

    private var fileAlert: Alert? = null

    override suspend fun noDestinationDirectory(context: SpiralContext) {
        javafx {
            val alert = Alert(Alert.AlertType.ERROR, "No destination directory provided ??", ButtonType.OK)
            alert.showAndWait()
        }
    }

    override suspend fun destinationNotDirectory(context: SpiralContext, destination: File) {
        javafx {
            val alert = Alert(Alert.AlertType.ERROR, "Destination is not a directory: $destination", ButtonType.OK)
            alert.showAndWait()
        }
    }

    override suspend fun beginFileAnalysis(context: SpiralContext, formats: List<ReadableSpiralFormat<SpiralArchive>>) {
        javaFX {
//            dialogStage = Stage()
//            dialogStage?.initStyle(StageStyle.UTILITY)
//            dialogStage?.isResizable = false
//            dialogStage?.initModality(Modality.APPLICATION_MODAL)
//
//            pb = ProgressBar()
//            pb?.progress = -1.0
//
//            val label = Label()
//            label.text = "Analysing Files"
//
//            val vb = VBox()
//            vb.spacing = 10.0
//            vb.alignment = Pos.CENTER
//            vb.children.addAll(label, pb)
//
//            val scene = Scene(vb)
//            dialogStage?.scene = scene
//            dialogStage?.show()

            fileAlert = Alert(Alert.AlertType.INFORMATION, "")
                .apply {
                    headerText = "Analysing Archive..."
                    graphic = ProgressIndicator()
                    buttonTypes.clear()
                    show()
                }
        }
    }

    override suspend fun noFormatForFile(context: SpiralContext, dataSource: DataSource<*>) {
        javafx {
            val confirmation = Alert(Alert.AlertType.ERROR, "Could not identify the archive", ButtonType.OK)
            confirmation.headerText = "Identification Failed"
            confirmation.showAndWait()
        }
    }

    override suspend fun foundFileFormat(context: SpiralContext, result: FormatResult<Optional<SpiralArchive>, SpiralArchive>, compressionFormats: List<ReadableCompressionFormat>?, archive: SpiralArchive) {
        javafx {
            val confirmation = Alert(Alert.AlertType.CONFIRMATION, "File is of type ${result.format().name} (${(result.confidence() * 10000).roundToInt() / 100}%)\nContinue Extraction?", ButtonType.YES, ButtonType.NO)
            confirmation.headerText = "File Identified"
            val buttonPressed = confirmation.showAndWait()
            println(buttonPressed)
        }
    }

    override suspend fun finishFileAnalysis(context: SpiralContext) {
        javafx { fileAlert?.close() }
    }

    override suspend fun archiveIsEmpty(context: SpiralContext, archive: SpiralArchive) {
        TODO("Not yet implemented")
    }

    override suspend fun beginExtracting(context: SpiralContext, archive: SpiralArchive, destination: File) {
        TODO("Not yet implemented")
    }

    override suspend fun beginExtractingSubfile(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>) {
        TODO("Not yet implemented")
    }

    override suspend fun subfileIsEmpty(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>) {
        TODO("Not yet implemented")
    }

    override suspend fun subfileHasNoMoreData(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>, waitCount: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun finishExtractingSubfile(context: SpiralContext, archive: SpiralArchive, destination: File, subfile: String, flow: InputFlow, source: DataSource<*>) {
        TODO("Not yet implemented")
    }

    override suspend fun finishExtracting(context: SpiralContext, archive: SpiralArchive, destination: File) {
        TODO("Not yet implemented")
    }


}