package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.objectTypeParameter
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.types.asString
import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.getOrNull
import dev.brella.kornea.base.common.use
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.Uri
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.populate
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.data.GurrenSpiralContext
import info.spiralframework.console.jvm.pipeline.DataSourceType
import info.spiralframework.console.jvm.pipeline.registerFunctionWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.bridgeFor
import info.spiralframework.core.common.formats.filterIsIdentifyFormatResult
import info.spiralframework.core.common.formats.populateForConversionSelection
import info.spiralframework.core.mapResults
import info.spiralframework.core.sortedAgainst
import info.spiralframework.formats.common.archives.SpiralArchive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalCoroutinesApi::class)
class GurrenConvertPilot(val readableFormats: MutableList<ReadableSpiralFormat<Any>>, val writableFormats: MutableList<WritableSpiralFormat>) : CoroutineScope {
    companion object : CommandRegistrar {
        const val NO_MATCHING_FORMAT_NAME = 0
        const val NO_FORMAT_IDENTIFIED = 1
        const val URI_IS_NOT_FILE = 2

        override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
            with(knolusContext) {
                registerFunctionWithContextWithoutReturn(
                    "convert",
                    objectTypeParameter("file_path"),
                    stringTypeParameter("from").asOptional(),
                    stringTypeParameter("to").asOptional(),
                    stringTypeParameter("save_as").asOptional()
                ) { context, filePath, from, to, saveAs ->
                    val spiralContext = context.spiralContext().getOrBreak { return@registerFunctionWithContextWithoutReturn }

                    if (filePath is DataSourceType) {
                        filePath.inner.use { ds ->
                            GurrenConvertPilot(spiralContext, ds, GurrenPilot.formatContext.let { formatContext ->
                                formatContext.withOptional(ISpiralProperty.FileName, ds.locationAsUri().getOrNull()?.path)
                            }, from, to, saveAs)
                        }
                    } else {
                        filePath.asString(knolusContext).doOnSuccess { filePath ->
                            convertStub(spiralContext, filePath, from, to, saveAs)
                        }
                    }
                }
            }

            GurrenPilot.help("convert")
        }

        suspend fun convertStub(context: GurrenSpiralContext, filePath: String, from: KorneaResult<String>, to: KorneaResult<String>, saveAs: KorneaResult<String>) {
            val file = File(filePath)

            if (!file.exists()) {
                context.printlnLocale("error.file.does_not_exist", filePath)
                return
            }

            if (file.isDirectory) {
                // Directory was passed; this is a potential ambiguity, so don't do anything here
                context.printlnLocale("commands.pilot.convert.err_path_is_directory", filePath)
                return
            } else if (file.isFile) {
                return AsyncFileDataSource(file).use { ds -> GurrenConvertPilot(context, ds, GurrenPilot.formatContext.with(ISpiralProperty.FileName, file.name), from, to, saveAs) }
            } else {
                context.printlnLocale("commands.pilot.convert.err_path_not_file_or_directory", filePath)
                return
            }
        }

        suspend operator fun invoke(context: GurrenSpiralContext, dataSource: DataSource<*>, readContext: SpiralProperties, from: KorneaResult<String>, to: KorneaResult<String>, saveAs: KorneaResult<String>) =
            GurrenConvertPilot(GurrenShared.READABLE_FORMATS, GurrenShared.WRITABLE_FORMATS)(context, dataSource, readContext, {}, from, to, saveAs.map { path ->
                Pair(AsyncFileOutputFlow(File(path)), GurrenPilot.formatContext.with(ISpiralProperty.FileName, path))
            }, GurrenPilot::formatContext.setter)

        suspend operator fun invoke(context: GurrenSpiralContext, data: Any, to: String, saveAs: String) =
            GurrenConvertPilot(GurrenShared.READABLE_FORMATS, GurrenShared.WRITABLE_FORMATS)(
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
        dataSource: DataSource<*>,
        readContext: SpiralProperties,
        updateReadContext: (SpiralProperties) -> Unit,
        from: KorneaResult<String>,
        to: KorneaResult<String>,
        saveAs: KorneaResult<Pair<OutputFlow, SpiralProperties>>,
        updateWriteProperties: (SpiralProperties) -> Unit
    ) {
        val (readingResult, readFormatFail) = from.map { name ->
            readableFormats.firstOrNull { format -> format.name.equals(name, true) }
            ?: readableFormats.firstOrNull { format -> format.extension?.equals(name, true) == true }
            ?: return noMatchingFormatName(name)
        }.flatMap { format ->
            format.identify(context, source = dataSource)
        }.switchIfEmpty {
            readableFormats.sortedAgainst(readContext)
                .mapResults { archive -> archive.identify(context, readContext, dataSource) }
                .filterIsInstance<FormatResult<Optional<SpiralArchive>, SpiralArchive>>()
                .sortedBy(FormatResult<*, *>::confidence)
                .asReversed()
                .firstOrNull()
            ?: KorneaResult.errorAsIllegalArgument(NO_FORMAT_IDENTIFIED, "No format identified from readable list")
        }.filterIsIdentifyFormatResult<Any>()

        if (readingResult == null) return formatDoesNotMatch(readFormatFail!!, dataSource)

        val writingFormat = to.map { name ->
            writableFormats.firstOrNull { format -> format.name.equals(name, true) }
            ?: writableFormats.firstOrNull { format -> format.extension?.equals(name, true) == true }
            ?: return noMatchingFormatName(name)
        }.switchIfEmpty {
            val readingFormat = readingResult.format()

            KorneaResult.successOrEmpty(
                readingFormat.preferredConversionFormat(
                    context,
                    context.populateForConversionSelection(readingFormat, readContext)
                )
            )
        }.getOrBreak { failure -> return noWritingFormat(readingResult, failure) }

        //TODO
        val readingData = KorneaResult.successOrEmpty(readingResult.get().getOrNull())
            .switchIfEmpty { readingResult.format().read(context, readContext, dataSource) }
            .getOrBreak { failure -> return readingFailed(readingResult, failure) }

        val (outputFlow, writeContext) = saveAs.getOrElseTransform { failure ->
            if (failure !is KorneaResult.Empty) return outputFailed(failure)

            val outputFile = dataSource.locationAsUri()
                .flatMapOrSelf { uri ->
                    if (uri.protocol == Uri.PROTOCOL_FILE) null
                    else KorneaResult.errorAsIllegalArgument(URI_IS_NOT_FILE, "Could not obtain an output location from $uri: Not a file location")
                }
                .getOrBreak { return cannotObtainOutputLocation(it) }
                .let { uri ->
                    File(buildString {
                        append(uri.path.substringBeforeLast('.'))
                        writingFormat.extension?.let {
                            append('.')
                            append(it)
                        }
                    })
                }

            Pair(AsyncFileOutputFlow(outputFile), GurrenPilot.formatContext.with(ISpiralProperty.FileName, outputFile.name))
        }

        invoke(context, readingData, writingFormat, outputFlow, writeContext, updateWriteProperties)
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

            println("Converting from ${readingData::class.simpleName} -> ${writingFormat.name} ($readingData), and saving to $outputFlow")

            val requiredProperties = bridge.requiredPropertiesForWritingAs(context, writeContext, writingFormat, readingData)

            if (requiredProperties.isNotEmpty()) {
//            println("Attempting to fill in ${requiredProperties.size} propert(y/ies)")

                formatContext = context.populate(writeContext, readingData, requiredProperties) ?: writeContext
            } else {
                formatContext = writeContext
            }

            val writeResult = arbitraryProgressBar(loadingText = "Converting...", loadedText = "Finished converting!") {
                outputFlow.use { bridge.writeAs(context, formatContext, writingFormat, readingData, it) }
            }

            println(writeResult)
        } else {
            println("Converting from ${readingData::class.simpleName} -> ${writingFormat.name} ($readingData), and saving to $outputFlow")

            val requiredProperties = writingFormat.requiredPropertiesForWrite(context, writeContext, readingData)

            if (requiredProperties.isNotEmpty()) {
//            println("Attempting to fill in ${requiredProperties.size} propert(y/ies)")

                formatContext = context.populate(writeContext, readingData, requiredProperties) ?: writeContext
            } else {
                formatContext = writeContext
            }

            val writeResult = arbitraryProgressBar(loadingText = "Converting...", loadedText = "Finished converting!") {
                outputFlow.use { writingFormat.write(context, formatContext, readingData, it) }
            }

            println(writeResult)
        }

        if (formatContext != writeContext) updateWriteProperties(formatContext.without(formatContext.keys().filterNot(ISpiralProperty.PropertyKey<*>::isPersistent)))
    }
}