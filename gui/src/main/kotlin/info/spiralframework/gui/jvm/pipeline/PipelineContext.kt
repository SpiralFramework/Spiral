package info.spiralframework.gui.jvm.pipeline

import info.spiralframework.antlr.pipeline.PipelineLexer
import info.spiralframework.antlr.pipeline.PipelineParser
import info.spiralframework.base.common.SpiralContext
import org.antlr.v4.runtime.CharStreams
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
class PipelineFunction<T>(
    val name: String,
    vararg val parameters: Pair<String, PipelineUnion.VariableValue?>,
    val variadicSupported: Boolean = false,
    val func: suspend (spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, PipelineUnion.VariableValue>) -> T
) {
    suspend fun suspendInvoke(
        spiralContext: SpiralContext,
        pipelineContext: PipelineContext,
        parameters: Map<String, PipelineUnion.VariableValue>
    ) = func(spiralContext, pipelineContext, parameters)
}

class FunctionBuilder<T>(val name: String) {
    val parameters: MutableList<Pair<String, PipelineUnion.VariableValue?>> = ArrayList()
    var variadicSupported = false
    lateinit var func: suspend (spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, PipelineUnion.VariableValue>) -> T

    fun addParameter(name: String, default: PipelineUnion.VariableValue? = null) {
        parameters.add(Pair(name.sanitiseFunctionIdentifier(), default))
    }

    fun addFlag(name: String, default: Boolean = false) =
        addParameter(name, PipelineUnion.VariableValue.BooleanType(default))

    fun setFunction(func: suspend (spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, PipelineUnion.VariableValue>) -> T) {
        this.func = func
    }

    fun build() = PipelineFunction(name, *parameters.toTypedArray(), variadicSupported = variadicSupported, func = func)
}

@ExperimentalUnsignedTypes
open class PipelineContext(val parent: PipelineContext?) {
    private val variableRegistry: MutableMap<String, PipelineUnion.VariableValue> = HashMap()
    private val functionRegistry: MutableMap<String, MutableList<PipelineFunction<PipelineUnion.VariableValue?>>> =
        HashMap()

    operator fun get(key: String): PipelineUnion.VariableValue? = variableRegistry[key] ?: parent?.get(key)

    operator fun set(key: String, global: Boolean = false, value: PipelineUnion.VariableValue) {
        if (global && parent != null) {
            parent[key, global] = value
        } else {
            variableRegistry[key] = value
        }
    }


    fun register(name: String, init: FunctionBuilder<PipelineUnion.VariableValue?>.() -> Unit) {
        val builder = FunctionBuilder<PipelineUnion.VariableValue?>(name)
        builder.init()
        register(name, builder.build())
    }

    fun register(
        name: String,
        func: suspend (spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, PipelineUnion.VariableValue>) -> PipelineUnion.VariableValue?,
        init: FunctionBuilder<PipelineUnion.VariableValue?>.() -> Unit
    ) {
        val builder = FunctionBuilder<PipelineUnion.VariableValue?>(name)
        builder.setFunction(func)
        builder.init()
        register(name, builder.build())
    }

    fun register(name: String, func: PipelineFunction<PipelineUnion.VariableValue?>, global: Boolean = false) {
        if (global && parent != null) {
            parent.register(name, func, global)
        } else {
            val functions: MutableList<PipelineFunction<PipelineUnion.VariableValue?>>
            if (name.sanitiseFunctionIdentifier() !in functionRegistry) {
                functions = ArrayList()
                functionRegistry[name.sanitiseFunctionIdentifier()] = functions
            } else {
                functions = functionRegistry.getValue(name.sanitiseFunctionIdentifier())
            }

            functions.add(func)
        }
    }

    suspend fun invokeScript(
        context: SpiralContext,
        scriptName: String,
        scriptParameters: Array<PipelineUnion.ScriptParameterType>
    ): PipelineUnion.VariableValue? = invokeFunction(
        context,
        scriptName,
        Array(scriptParameters.size) { i ->
            PipelineUnion.FunctionParameterType(
                scriptParameters[i].name,
                scriptParameters[i].parameter
            )
        })

    suspend fun invokeFunction(
        context: SpiralContext,
        functionName: String,
        functionParameters: Array<PipelineUnion.FunctionParameterType>
    ): PipelineUnion.VariableValue? {
        val flattened = functionParameters.map { (name, value) ->
            if (name != null) PipelineUnion.FunctionParameterType(name, value) else PipelineUnion.FunctionParameterType(
                null,
                value.flatten(context, this)
            )
        }

        val pipelineContext = this

        with(context) {
            trace(
                "Calling $functionName(${
                    flattened.map { value ->
                        "${value.name}=${
                            value.parameter.asString(
                                context,
                                pipelineContext
                            )
                        }"
                    }.joinToString()
                })"
            )
        }

        val flatPassed = flattened.count()
        val function = functionRegistry[functionName.sanitiseFunctionIdentifier()]
            ?.firstOrNull { func -> (flatPassed >= func.parameters.count { (_, default) -> default == null } && flatPassed <= func.parameters.size) || (func.variadicSupported && flatPassed >= func.parameters.size) }
            ?: return parent?.invokeFunction(context, functionName, flattened.toTypedArray())

        val functionParams = function.parameters.toMutableList()
        val passedParams: MutableMap<String, PipelineUnion.VariableValue> = HashMap()
        flattened.filter { union ->
            //            if (union !is PipelineUnion.FunctionParameterType) return@forEach
            val parameter = functionParams.firstOrNull { (p) ->
                p == union.name?.sanitiseFunctionIdentifier()
            } ?: return@filter true

            passedParams[parameter.first] = union.parameter
            functionParams.remove(parameter)

            false
        }.forEach { union ->
            passedParams[union.name?.sanitiseFunctionIdentifier()
                ?: if (functionParams.isNotEmpty()) functionParams.removeAt(0).first else "INDEX${passedParams.size}"] =
                union.parameter
        }

        functionParams.forEach { (name, default) -> passedParams.putIfAbsent(name, default ?: return@forEach) }

        return function.suspendInvoke(context, this, passedParams)
    }

    init {
        PipelineFunctions.registerAll(this)
    }
}

@ExperimentalUnsignedTypes
suspend fun PipelineUnion.ScopeType.run(
    spiralContext: SpiralContext,
    parentContext: PipelineContext? = null,
    parameters: Map<String, Any?> = emptyMap()
): PipelineUnion.VariableValue? {
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

private val SEPARATOR_CHARACTERS = "[_\\- ]".toRegex()
private fun String.sanitiseFunctionIdentifier(): String =
    uppercase(Locale.getDefault()).replace(SEPARATOR_CHARACTERS, "")