package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.stringTypeParameter
import dev.brella.kornea.errors.common.Optional
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.common.use
import dev.brella.kornea.toolkit.coroutines.ascii.createArbitraryProgressBar
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.ReadableCompressionFormat
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.panels.IdentifyCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

class GurrenIdentifyPilot(override val identifiableFormats: List<ReadableSpiralFormat<*>>) : IdentifyCommand, CoroutineScope {
    companion object : CommandRegistrar {
        override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
            with(knolusContext) {
                registerFunctionWithContextWithoutReturn("identify", stringTypeParameter("file_path")) { context, filePath ->
                    context.spiralContext().doOnSuccess { identifyStub(it, context, filePath) }
                }
            }

            GurrenPilot.help("identify")
            GurrenPilot.help("identify" to listOf("identification", "identify_file", "identify_files", "identify_file_format"))
        }

        suspend inline fun identifyStub(spiralContext: SpiralContext, knolusContext: KnolusContext, filePath: String) = identifyStub(spiralContext, knolusContext, GurrenPilot.formatContext.withOptional(ISpiralProperty.FileName, filePath), filePath)
        suspend inline fun identifyStub(spiralContext: SpiralContext, knolusContext: KnolusContext, readContext: SpiralProperties, filePath: String) =
            AsyncFileDataSource(File(filePath)).use { identifyStub(spiralContext, knolusContext, readContext, it) }

        suspend fun identifyStub(spiralContext: SpiralContext, knolusContext: KnolusContext, readContext: SpiralProperties, dataSource: DataSource<*>) {
            spiralContext.printlnLocale("commands.pilot.identify.begin", readContext[ISpiralProperty.FileName] ?: spiralContext.constNull())

            GurrenIdentifyPilot(GurrenShared.READABLE_FORMATS)(spiralContext, readContext, dataSource)
        }
    }

    override val coroutineContext: CoroutineContext = SupervisorJob()

    private var fileAnalysisProgressBar: Job? = null

    override suspend fun beginIdentification(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>, formats: List<ReadableSpiralFormat<*>>) {
        fileAnalysisProgressBar = createArbitraryProgressBar(loadingText = context.localise("commands.pilot.identify.identifying"), loadedText = context.localise("commands.pilot.identify.identified"))
    }

    override suspend fun noFormatFound(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>) {
        context.printlnLocale("commands.pilot.identify.err_no_format_for", dataSource.location ?: context.constNull())
    }

    override suspend fun foundFileFormat(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>, result: FormatResult<Optional<*>, *>, compressionFormats: List<ReadableCompressionFormat>?) {
        context.printlnLocale("commands.pilot.identify.format_is", (result.confidence() * 10000).roundToInt() / 100.0, dataSource.location ?: context.constNull(), result.format().name)
    }

    override suspend fun finishIdentification(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>) {
        fileAnalysisProgressBar?.cancelAndJoin()
        fileAnalysisProgressBar = null

        print('\r')
    }
}