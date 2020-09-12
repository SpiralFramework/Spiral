package info.spiralframework.console.jvm.pipeline

import dev.brella.knolus.booleanTypeParameter
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunction
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.modules.functionregistry.registerFunctionWithoutReturn
import dev.brella.knolus.numberTypeAsIntParameter
import dev.brella.knolus.objectTypeAsStringParameter
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.types.KnolusArray
import dev.brella.knolus.types.KnolusConstants
import dev.brella.kornea.errors.common.getOrElse
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.errors.common.orElse
import dev.brella.kornea.toolkit.common.mapToArray
import info.spiralframework.base.jvm.URLDataSource
import java.io.File
import java.net.MalformedURLException
import java.net.URL

@ExperimentalUnsignedTypes
object PipelineFunctions {
    fun registerAll(pipelineContext: KnolusContext) {
        with(pipelineContext) {
            registerFunctionWithoutReturn("println", objectTypeAsStringParameter("line")) { line -> println(line) }

            registerFunction(
                "sub_files",
                stringTypeParameter("path"),
                stringTypeParameter("filter").asOptional(),
                stringTypeParameter("extension").asOptional(),
                numberTypeAsIntParameter("max_depth").asOptional(),
                booleanTypeParameter("shallow") withDefault false
            ) { path, filter, extension, maxDepth, shallow ->
                val maxDepth = maxDepth.getOrElse(if (shallow) 1 else Int.MAX_VALUE)

                val filter = filter.getOrElse(".+")
                val regex: Regex = extension.map { ext -> "$filter\\.$ext".toRegex() }
                    .getOrElse(filter.toRegex())

                KnolusArray.ofStable(
                    File(path)
                        .walk()
                        .maxDepth(maxDepth)
                        .filter { file -> file.isFile }
                        .filter { file -> file.absolutePath.matches(regex) }
                        .toList()
                        .mapToArray { file -> wrap(file) }
                )
            }

            registerFunction("url", stringTypeParameter("url")) { urlPath ->
                try {
                    return@registerFunction DataSourceType(URLDataSource(URL(urlPath)))
                } catch (malformed: MalformedURLException) {
                    return@registerFunction KnolusConstants.Null
                }
            }
    }
}
}