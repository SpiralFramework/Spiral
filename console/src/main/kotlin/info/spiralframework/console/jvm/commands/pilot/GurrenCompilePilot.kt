package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.stringTypeParameter
import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.use
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import dev.brella.zshk.common.ShellEnvironment
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.populate
import info.spiralframework.base.jvm.io.files.Folder
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.data.GurrenSpiralContext
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.bridgeFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.coroutines.CoroutineContext

class GurrenCompilePilot(val readableFormats: MutableList<ReadableSpiralFormat<Any>>, val writableFormats: MutableList<WritableSpiralFormat>) : CoroutineScope {
    public companion object : CommandRegistrar {
        public const val NO_MATCHING_FORMAT_NAME: Int = 0
        public const val NO_FORMAT_IDENTIFIED: Int = 1
        public const val URI_IS_NOT_FILE: Int = 2

        override suspend fun register(spiralContext: SpiralContext, env: ShellEnvironment) {
//            env.registerFunction("compile") { args, env ->
//            }

                registerFunctionWithContextWithoutReturn(
                    "compile",
                    stringTypeParameter("path"),
                    stringTypeParameter("to"),
                    stringTypeParameter("save_as")
                ) { context, path, to, saveAs ->
                    val spiralContext = context.spiralContext().getOrBreak { return@registerFunctionWithContextWithoutReturn }

                    GurrenCompilePilot(spiralContext, Folder(File(path)), to, saveAs)
                }

            GurrenPilot.help("compile")
        }

        suspend operator fun invoke(context: GurrenSpiralContext, data: Any, to: String, saveAs: String) =
            GurrenCompilePilot(GurrenShared.READABLE_FORMATS, GurrenShared.WRITABLE_FORMATS)(
                context,
                data,
                to,
                AsyncFileOutputFlow(File(saveAs)),
                GurrenPilot.formatContext.with(ISpiralProperty.FileName, saveAs.substringAfterLast('/').substringAfterLast('\\')),
                GurrenPilot::formatContext.setter
            )
    }

    override val coroutineContext: CoroutineContext = SupervisorJob()

    suspend fun noMatchingFormatName(formatName: String) {
        println("No matching format with name '$formatName'")
    }

    suspend fun formatDoesNotMatch(formatError: KorneaResult<*>, dataSource: DataSource<*>) {
        println("Format error: $formatError / $dataSource")
    }

    suspend fun noWritingFormat(readingResult: FormatResult<Optional<Any>, Any>, failure: KorneaResult.Failure) {
        println("No writing format: $failure")
    }

    suspend fun readingFailed(readingResult: FormatResult<Optional<Any>, Any>, failure: KorneaResult.Failure) {
        println("Could not read ${readingResult.format().name}: $failure")
    }

    suspend fun outputFailed(failure: KorneaResult.Failure) {
        println("Output failed: $failure")
    }

    suspend fun cannotObtainOutputLocation(failure: KorneaResult.Failure) {
        println("Failed to obtain an output location: $failure")
    }

    suspend fun formatDoesNotSupportWriting(writingFormat: WritableSpiralFormat, writeContext: SpiralProperties?, readingData: Any) {
        println("${writingFormat.name} does not support writing this type of data (${readingData::class.simpleName})")
    }

    suspend operator fun invoke(
        context: GurrenSpiralContext,
        readingData: Any,
        to: String,
        outputFlow: OutputFlow,
        writeContext: SpiralProperties,
        updateWriteProperties: (SpiralProperties) -> Unit
    ) {
        val writingFormat =
            writableFormats.firstOrNull { format -> format.name.equals(to, true) }
            ?: writableFormats.firstOrNull { format -> format.extension?.equals(to, true) == true }
            ?: return noMatchingFormatName(to)

        invoke(context, readingData, writingFormat, outputFlow, writeContext, updateWriteProperties)
    }

    suspend operator fun invoke(
        context: GurrenSpiralContext,
        readingData: Any,
        writingFormat: WritableSpiralFormat,
        outputFlow: OutputFlow,
        writeContext: SpiralProperties,
        updateWriteProperties: (SpiralProperties) -> Unit
    ) {
        val formatContext: SpiralProperties?

        if (!writingFormat.supportsWriting(context, writeContext, readingData)) {
            val bridge = context.bridgeFor(writingFormat, writeContext, readingData)
                         ?: return formatDoesNotSupportWriting(writingFormat, writeContext, readingData)

            println("Compiling to ${writingFormat.name}, and saving to $outputFlow")

            val requiredProperties = bridge.requiredPropertiesForWritingAs(context, writeContext, writingFormat, readingData)

            if (requiredProperties.isNotEmpty()) {
//            println("Attempting to fill in ${requiredProperties.size} propert(y/ies)")

                formatContext = context.populate(writeContext, readingData, requiredProperties) ?: writeContext
            } else {
                formatContext = writeContext
            }

            arbitraryProgressBar(loadingText = "Compiling...", loadedText = "Finished compiling!") {
                outputFlow.use { bridge.writeAs(context, formatContext, writingFormat, readingData, it) }
            }
        } else {
            println("Compiling to ${writingFormat.name}, and saving to $outputFlow")

            val requiredProperties = writingFormat.requiredPropertiesForWrite(context, writeContext, readingData)

            if (requiredProperties.isNotEmpty()) {
//            println("Attempting to fill in ${requiredProperties.size} propert(y/ies)")

                formatContext = context.populate(writeContext, readingData, requiredProperties) ?: writeContext
            } else {
                formatContext = writeContext
            }

            arbitraryProgressBar(loadingText = "Compiling...", loadedText = "Finished compiling!") {
                outputFlow.use { writingFormat.write(context, formatContext, readingData, it) }
            }
        }

        if (formatContext != writeContext) updateWriteProperties(formatContext.without(formatContext.keys().filterNot(ISpiralProperty.PropertyKey<*>::isPersistent)))
    }
}