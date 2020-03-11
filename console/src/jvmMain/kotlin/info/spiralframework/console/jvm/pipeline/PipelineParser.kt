package info.spiralframework.console.jvm.pipeline

import info.spiralframework.antlr.pipeline.PipelineLexer
import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralSuspending
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree.Tree
import org.antlr.v4.runtime.tree.Trees
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

fun toStringTree(t: Tree, recog: Parser?): String? {
    val ruleNames = recog?.ruleNames
    val ruleNamesList = if (ruleNames != null) listOf(*ruleNames) else null
    return toStringTree(t, ruleNamesList)
}

/** Print out a whole tree in LISP form. [.getNodeText] is used on the
 * node payloads to get the text for the nodes.
 */
fun toStringTree(t: Tree, ruleNames: List<String?>?, indent: Int = 0): String? {
    var s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    if (t.childCount == 0) return s
    val buf = StringBuilder()
//    buf.append("(")
    buf.appendln()
    repeat(indent) { buf.append('\t') }
    buf.append("> ")
    s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    buf.append(s)
    buf.append(' ')
    for (i in 0 until t.childCount) {
        if (i > 0) buf.append(' ')
        buf.append(toStringTree(t.getChild(i), ruleNames, indent + 1))
    }
//    buf.append(")")
    return buf.toString()
}

fun parsePipeline(text: String): PipelineUnion.ScopeType {
    val charStream = CharStreams.fromString(text)
    val lexer = PipelineLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = PipelineParser(tokens)
    val visitor = PipelineVisitor()
    val pipeline = visitor.visitScope(parser.file().scope())
    return pipeline
}

@ExperimentalUnsignedTypes
class PipelineFunction<T>(name: String, vararg parameterNames: String, val variadicSupported: Boolean = false, val func: suspend (spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, Any?>) -> T) : SpiralSuspending.Function<T>(name, parameterNames) {
    override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters.getValue("pipeline_context") as PipelineContext, parameters)
    suspend fun suspendInvoke(spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, Any?>) = func(spiralContext, pipelineContext, parameters)
}

@ExperimentalUnsignedTypes
class PipelineContext(val parent: PipelineContext?) {
    private val variableRegistry: MutableMap<String, PipelineUnion.VariableValue> = HashMap()
    private val functionRegistry: MutableMap<String, MutableList<PipelineFunction<PipelineUnion.VariableValue?>>> = HashMap()

    operator fun get(key: String): PipelineUnion.VariableValue? = variableRegistry[key] ?: parent?.get(key)

    operator fun set(key: String, global: Boolean = false, value: PipelineUnion.VariableValue) {
        if (global && parent != null) {
            parent[key, global] = value
        } else {
            variableRegistry[key] = value
        }
    }

    fun register(name: String, vararg parameterNames: String, variadicSupported: Boolean = false, global: Boolean = false, func: suspend (spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, Any?>) -> PipelineUnion.VariableValue?) =
            register(name, PipelineFunction(name, *parameterNames, variadicSupported = variadicSupported, func = func), global)

    fun register(name: String, func: PipelineFunction<PipelineUnion.VariableValue?>, global: Boolean = false) {
        if (global && parent != null) {
            parent.register(name, func, global)
        } else {
            val functions: MutableList<PipelineFunction<PipelineUnion.VariableValue?>>
            if (name.toUpperCase().replace("_", "") !in functionRegistry) {
                functions = ArrayList()
                functionRegistry[name.toUpperCase().replace("_", "")] = functions
            } else {
                functions = functionRegistry.getValue(name.toUpperCase().replace("_", ""))
            }

            functions.add(func)
        }
    }

    suspend fun invokeScript(context: SpiralContext, scriptName: String, scriptParameters: Array<PipelineUnion.ScriptParameterType>): PipelineUnion.VariableValue? = invokeFunction(context, scriptName, Array(scriptParameters.size) { i -> PipelineUnion.FunctionParameterType(scriptParameters[i].name, scriptParameters[i].parameter) })
    suspend fun invokeFunction(context: SpiralContext, functionName: String, functionParameters: Array<PipelineUnion.FunctionParameterType>): PipelineUnion.VariableValue? {
        val flattened = functionParameters.map { (name, value) ->
            if (name != null) PipelineUnion.FunctionParameterType(name, value) else PipelineUnion.FunctionParameterType(null, value.flatten(context, this))
        }

        val pipelineContext = this

        with(context) {
            trace("Calling $functionName(${flattened.map { value -> "${value.name}=${value.parameter.asString(context, pipelineContext)}" }.joinToString()})")
        }

        val function = functionRegistry[functionName.toUpperCase().replace("_", "")]
                ?.firstOrNull { func -> func.parameterNames.size == flattened.size || (func.variadicSupported && flattened.size >= func.parameterNames.size) }
                ?: return parent?.invokeFunction(context, functionName, flattened.toTypedArray())

        val functionParams = function.parameterNames.toMutableList()
        val passedParams: MutableMap<String, Any?> = HashMap()
        flattened.forEach { union ->
//            if (union !is PipelineUnion.FunctionParameterType) return@forEach
            val parameter = functionParams.firstOrNull { p -> p == union.name } ?: return@forEach
            passedParams[parameter] = union.parameter
            functionParams.remove(parameter)
        }

        flattened.forEach { union ->
            passedParams[if (functionParams.isNotEmpty()) functionParams.removeAt(0) else "index_${passedParams.size}"] = union.parameter
        }

        return function.suspendInvoke(context, this, passedParams)
    }
}

@ExperimentalUnsignedTypes
suspend fun PipelineUnion.ScopeType.run(spiralContext: SpiralContext, parentContext: PipelineContext? = null, parameters: Map<String, Any?> = emptyMap()): PipelineUnion.VariableValue? {
    val pipelineContext = PipelineContext(parentContext)
    parameters.forEach { (k, v) -> pipelineContext[k] = v as? PipelineUnion.VariableValue ?: return@forEach }

    lines.forEach { union ->
        when (union) {
            is PipelineUnion.Action -> union.run(spiralContext, pipelineContext)
            is PipelineUnion.ReturnStatement -> return union.value.flatten(spiralContext, pipelineContext)
        }
    }

    return null
}