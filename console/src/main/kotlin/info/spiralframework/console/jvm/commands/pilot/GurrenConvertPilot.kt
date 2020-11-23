package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.objectTypeParameter
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.types.asString
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.common.use
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.pipeline.DataSourceType
import info.spiralframework.console.jvm.pipeline.registerFunctionWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.common.formats.FormatResult
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.filterIsIdentifyFormatResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.coroutines.CoroutineContext

class GurrenConvertPilot(val readableFormats: MutableList<ReadableSpiralFormat<out Any>>, val writableFormats: MutableList<WritableSpiralFormat>) : CoroutineScope {
    companion object : CommandRegistrar {
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
                        filePath.inner.use { ds -> GurrenConvertPilot(spiralContext, ds, from, to, saveAs) }
                    } else {
                        filePath.asString(knolusContext).doOnSuccess { filePath ->
                            convertStub(spiralContext, filePath, from, to, saveAs)
                        }
                    }
                }
            }

            GurrenPilot.help("convert")
        }

        suspend fun convertStub(context: SpiralContext, filePath: String, from: KorneaResult<String>, to: KorneaResult<String>, saveAs: KorneaResult<String>) {
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
                return AsyncFileDataSource(file).use { ds -> GurrenConvertPilot(context, ds, from, to, saveAs) }
            } else {
                context.printlnLocale("commands.pilot.convert.err_path_not_file_or_directory", filePath)
                return
            }
        }

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>, from: KorneaResult<String>, to: KorneaResult<String>, saveAs: KorneaResult<String>) =
            GurrenConvertPilot(GurrenShared.READABLE_FORMATS, GurrenShared.WRITABLE_FORMATS)
    }

    override val coroutineContext: CoroutineContext = SupervisorJob()

    suspend fun noMatchingFormatName(formatName: String) {

    }

    suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>, from: KorneaResult<String>, to: KorneaResult<String>, saveAs: KorneaResult<String>) {
        val readingFormat = from.map { name ->
            readableFormats.firstOrNull { format -> format.name.equals(name, true) }
            ?: readableFormats.firstOrNull { format -> format.extension?.equals(name, true) == true }
            ?: return noMatchingFormatName(name)
        }.flatMap { format ->
            format.identify(context, source = dataSource)
        }.filterIsIdentifyFormatResult<Any>()
    }
}