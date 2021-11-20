package info.spiralframework.gui.jvm.pipeline

import info.spiralframework.base.jvm.URLDataSource
import java.io.File
import java.net.MalformedURLException
import java.net.URL

@ExperimentalUnsignedTypes
object PipelineFunctions {
    fun registerAll(pipelineContext: PipelineContext) {
        with(pipelineContext) {
            register("println") {
                addParameter("line")

                setFunction { spiralContext, pipelineContext, parameters ->
                    println(parameters.getValue("LINE").asString(spiralContext, pipelineContext))

                    null
                }
            }

            register("sub_files") {
                addParameter("path")
                addParameter("filter", PipelineUnion.VariableValue.NullType)
                addParameter("extension", PipelineUnion.VariableValue.NullType)
                addParameter("max_depth", PipelineUnion.VariableValue.NullType)

                setFunction { spiralContext, pipelineContext, parameters ->
                    val path = parameters.getValue("PATH")
                            .flatten(spiralContext, pipelineContext)
                            .asString(spiralContext, pipelineContext)
                    val filter = parameters["FILTER"]?.asFlattenedStringIfPresent(spiralContext, pipelineContext)
                    val extension = parameters["EXTENSION"]?.asFlattenedStringIfPresent(spiralContext, pipelineContext)

                    val maxDepth = parameters["MAXDEPTH"]
                            ?.flatten(spiralContext, pipelineContext)
                            ?.takeIfPresent()
                            ?.asNumber(spiralContext, pipelineContext)
                            ?: if (parameters["SHALLOW"]?.asBoolean(spiralContext, pipelineContext) == true) 1 else Int.MAX_VALUE

                    val regex: Regex
                    if (extension != null)
                        regex = "${filter ?: ".+"}\\.$extension".toRegex()
                    else if (filter == null)
                        regex = ".+".toRegex()
                    else
                        regex = filter.toRegex()

                    PipelineUnion.VariableValue.ArrayType(
                            File(path)
                                    .walk()
                                    .maxDepth(maxDepth.toInt())
                                    .filter { file -> file.isFile }
                                    .map(File::getAbsolutePath)
                                    .filter { str -> str.matches(regex) }
                                    .map(PipelineUnion.VariableValue::StringType)
                                    .toList()
                                    .toTypedArray()
                    )
                }
            }

            register("url") {
                addParameter("url")

                setFunction { spiralContext, pipelineContext, parameters ->
                    val urlPath = parameters.getValue("URL")
                            .asFlattenedStringIfPresent(spiralContext, pipelineContext)
                            ?: return@setFunction PipelineUnion.VariableValue.NullType
                    try {
                        val url = URL(urlPath)

                        return@setFunction PipelineUnion.VariableValue.DataSourceType(URLDataSource(url))
                    } catch (malformed: MalformedURLException) {
                        return@setFunction PipelineUnion.VariableValue.NullType
                    }
                }
            }
        }
    }
}