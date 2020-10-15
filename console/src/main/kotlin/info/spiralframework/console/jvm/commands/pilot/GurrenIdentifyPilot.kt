package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.stringTypeParameter
import dev.brella.kornea.errors.common.Optional
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.common.use
import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cache
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.logging.trace
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.pilot.GurrenExtractFilesPilot.extractFiles
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.mapResults
import info.spiralframework.console.jvm.pipeline.registerFunctionWithContextWithoutReturn
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.DefaultFormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.formats.common.archives.SpiralArchive
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

object GurrenIdentifyPilot : CommandRegistrar {
    suspend fun SpiralContext.identify(knolusContext: KnolusContext, filePath: String) {
        println("Identifying '$filePath'...")

        AsyncFileDataSource(File(filePath)).use { baseDataSource ->
            val (decompressedDataSource, archiveCompressionFormats) = if (baseDataSource.reproducibility.isUnreliable() || baseDataSource.reproducibility.isUnstable()) {
                baseDataSource.cache(this).use { ds -> decompress(ds) }
            } else {
                decompress(baseDataSource)
            }

            val readContext = DefaultFormatReadContext(decompressedDataSource.location?.replace("$(.+?)(?:\\+[0-9a-fA-F]+h|\\[[0-9a-fA-F]+h,\\s*[0-9a-fA-F]+h\\])".toRegex()) { result -> result.groupValues[1] }, GurrenPilot.game)
            val result = arbitraryProgressBar(loadingText = localise("commands.pilot.extract_files.analysing_archive"), loadedText = null) {
                GurrenShared.READABLE_FORMATS.sortedBy { format -> abs(format.extension?.compareTo(readContext.name?.substringAfterLast('.') ?: "") ?: -100) }
                    .mapResults { archive -> archive.identify(this, readContext, decompressedDataSource) }
                    .also { results ->
                        trace {
                            trace("\rResults for \"{0}\":", baseDataSource.location ?: constNull())
                            results.forEachIndexed { index, result ->
                                trace("\t{0}] == {1} ==", GurrenShared.EXTRACTABLE_ARCHIVES[index].name, result)
                            }
                        }
                    }
                    .filterIsInstance<FormatResult<Optional<*>, *>>()
                    .sortedBy(FormatResult<*, *>::confidence)
                    .asReversed()
                    .firstOrNull()
            }

            print("\r                                                                            \r")

            if (result == null) {
                printlnLocale("commands.pilot.extract_files.err_no_format_for", baseDataSource.location ?: constNull())
                return
            }

            println("There's a ${(result.confidence() * 10000).roundToInt() / 100.0}% chance that that file is a ${result.format().name} file")
        }
    }

    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        with(knolusContext) {
            registerFunctionWithContextWithoutReturn("identify", stringTypeParameter("file_path")) { context, filePath ->
                context.spiralContext().doOnSuccess { it.identify(context, filePath) }
            }
        }
    }
}