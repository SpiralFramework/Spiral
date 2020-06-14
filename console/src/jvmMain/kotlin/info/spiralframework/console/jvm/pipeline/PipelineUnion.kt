package info.spiralframework.console.jvm.pipeline

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.text.toIntBaseN
import info.spiralframework.base.common.text.toIntOrNullBaseN
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.kornea.toolkit.common.takeIf

@ExperimentalUnsignedTypes
sealed class PipelineUnion {
    interface Action {
        suspend fun run(spiralContext: SpiralContext, pipelineContext: PipelineContext)
    }

    sealed class StringComponent : PipelineUnion() {
        data class RawText(val text: String) : StringComponent()
        data class VariableReference(val variableName: String) : StringComponent()
    }

    sealed class VariableValue : PipelineUnion() {
        abstract suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String
        abstract suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number
        abstract suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue
        abstract suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean

        data class StringComponents(val components: Array<StringComponent>) : VariableValue() {
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): StringType =
                    StringType(
                            components.mapNotNull { component ->
                                when (component) {
                                    is StringComponent.RawText -> component.text
                                    is StringComponent.VariableReference -> pipelineContext[component.variableName]?.asString(spiralContext, pipelineContext)
                                }
                            }.joinToString("")
                    )

            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = flatten(spiralContext, pipelineContext).string
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number {
                val str = flatten(spiralContext, pipelineContext).string
                if (str.contains('.'))
                    return str.toDouble()
                return str.toIntBaseN()
            }
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean {
                val str = asString(spiralContext, pipelineContext)
                if (str.equals("true", true) || str.equals("false", true))
                    return str.toBoolean()
                return (str.toIntOrNullBaseN() ?: 0) != 0
            }
        }

        data class StringType(val string: String) : VariableValue() {
            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = string
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = if (string.contains('.')) string.toDouble() else string.toIntBaseN()
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = this
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean {
                val str = asString(spiralContext, pipelineContext)
                if (str.equals("true", true) || str.equals("false", true))
                    return str.toBoolean()
                return (str.toIntOrNullBaseN() ?: 0) != 0
            }
        }

        data class BooleanType(val boolean: Boolean) : VariableValue() {
            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = boolean.toString()
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = if (boolean) 1 else 0
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = this

            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean = boolean
        }

        data class IntegerType(val integer: Int) : VariableValue() {
            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = integer.toString()
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = integer
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = this

            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean = integer != 0
        }

        data class DecimalType(val decimal: Double) : VariableValue() {
            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = decimal.toString()
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = decimal
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = this
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean = decimal.toInt() != 0
        }

        data class VariableReferenceType(val variableName: String) : VariableValue() {
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = pipelineContext[variableName]?.flatten(spiralContext, pipelineContext)
                    ?: NullType

            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = flatten(spiralContext, pipelineContext).asString(spiralContext, pipelineContext)
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = flatten(spiralContext, pipelineContext).asNumber(spiralContext, pipelineContext)
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean = flatten(spiralContext, pipelineContext).asBoolean(spiralContext, pipelineContext)
        }

        data class WrappedFunctionCallType(val name: String, val parameters: Array<FunctionParameterType>) : VariableValue() {
            constructor(functionCall: FunctionCallAction) : this(functionCall.name, functionCall.parameters)

            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = pipelineContext.invokeFunction(spiralContext, name, parameters)?.flatten(spiralContext, pipelineContext)
                    ?: NullType

            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = flatten(spiralContext, pipelineContext).asString(spiralContext, pipelineContext)
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = flatten(spiralContext, pipelineContext).asNumber(spiralContext, pipelineContext)
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean = flatten(spiralContext, pipelineContext).asBoolean(spiralContext, pipelineContext)
        }

        data class WrappedScriptCallType(val name: String, val parameters: Array<ScriptParameterType>) : VariableValue() {
            constructor(scriptCall: ScriptCallAction) : this(scriptCall.name, scriptCall.parameters)

            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = pipelineContext.invokeScript(spiralContext, name, parameters)?.flatten(spiralContext, pipelineContext)
                    ?: NullType

            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = flatten(spiralContext, pipelineContext).asString(spiralContext, pipelineContext)
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = flatten(spiralContext, pipelineContext).asNumber(spiralContext, pipelineContext)
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean = flatten(spiralContext, pipelineContext).asBoolean(spiralContext, pipelineContext)
        }

        data class ArrayType<T: VariableValue>(val array: Array<T>): VariableValue() {
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): ArrayType<VariableValue> =
                    ArrayType(Array(array.size) { i -> array[i].flatten(spiralContext, pipelineContext) })

            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean =
                    array.isNotEmpty()

            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number =
                    array.size

            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String =
                    flatten(spiralContext, pipelineContext)
                            .array
                            .map { t -> t.asString(spiralContext, pipelineContext) }
                            .joinToString(prefix = "arrayOf(", postfix = ")") { "\"$it\"" }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ArrayType<*>

                if (!array.contentEquals(other.array)) return false

                return true
            }

            override fun hashCode(): Int {
                return array.contentHashCode()
            }
        }

        data class DataSourceType<T: InputFlow>(val dataSource: DataSource<T>): VariableValue() {
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean =
                    dataSource.canOpenInputFlow()

            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number =
                    dataSource.dataSize?.toLong() ?: 0L

            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String =
                    dataSource.toString()

            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue =
                    this
        }

        object NullType : VariableValue() {
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number = 0
            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String = "null"
            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue = this
            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean = false
        }

        data class ExpressionType(val startValue: VariableValue, val ops: Array<Pair<ExpressionOperation, VariableValue>>) : VariableValue() {
            override suspend fun asNumber(spiralContext: SpiralContext, pipelineContext: PipelineContext): Number =
                    flatten(spiralContext, pipelineContext).asNumber(spiralContext, pipelineContext)

            override suspend fun asString(spiralContext: SpiralContext, pipelineContext: PipelineContext): String =
                    flatten(spiralContext, pipelineContext).asString(spiralContext, pipelineContext)

            override suspend fun asBoolean(spiralContext: SpiralContext, pipelineContext: PipelineContext): Boolean =
                    flatten(spiralContext, pipelineContext).asBoolean(spiralContext, pipelineContext)

            override suspend fun flatten(spiralContext: SpiralContext, pipelineContext: PipelineContext): VariableValue =
                    ops.fold(startValue) { first, (operation, second) -> operation.operate(spiralContext, pipelineContext, first, second) }
                            .flatten(spiralContext, pipelineContext)
        }
    }

    sealed class ExpressionOperation : PipelineUnion() {
        object PLUS : ExpressionOperation() {
            override suspend fun _operate(spiralContext: SpiralContext, pipelineContext: PipelineContext, first: VariableValue, second: VariableValue): VariableValue = when (first) {
                is VariableValue.StringComponents -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).plus(second.asString(spiralContext, pipelineContext)))
                is VariableValue.StringType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).plus(second.asString(spiralContext, pipelineContext)))
                is VariableValue.BooleanType -> VariableValue.BooleanType(first.boolean.xor(second.asNumber(spiralContext, pipelineContext).toInt() != 0))
                is VariableValue.IntegerType -> VariableValue.IntegerType(first.integer + second.asNumber(spiralContext, pipelineContext).toInt())
                is VariableValue.DecimalType -> VariableValue.DecimalType(first.decimal + second.asNumber(spiralContext, pipelineContext).toDouble())
//                is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        object MINUS : ExpressionOperation() {
            override suspend fun _operate(spiralContext: SpiralContext, pipelineContext: PipelineContext, first: VariableValue, second: VariableValue): VariableValue = when (first) {
                is VariableValue.StringComponents -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).removeSuffix(second.asString(spiralContext, pipelineContext)))
                    is VariableValue.StringType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).removeSuffix(second.string))
                    is VariableValue.BooleanType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).removeSuffix(second.boolean.toString()))
                    is VariableValue.IntegerType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).dropLast(second.integer))
                    is VariableValue.DecimalType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).dropLast(second.decimal.toInt()))
//                    is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.StringType -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(first.string.removeSuffix(second.asString(spiralContext, pipelineContext)))
                    is VariableValue.StringType -> VariableValue.StringType(first.string.removeSuffix(second.string))
                    is VariableValue.BooleanType -> VariableValue.StringType(first.string.removeSuffix(second.boolean.toString()))
                    is VariableValue.IntegerType -> VariableValue.StringType(first.string.dropLast(second.integer))
                    is VariableValue.DecimalType -> VariableValue.StringType(first.string.dropLast(second.decimal.toInt()))
//                    is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.BooleanType -> VariableValue.BooleanType(first.boolean.xor(second.asNumber(spiralContext, pipelineContext).toInt() != 0))
                is VariableValue.IntegerType -> VariableValue.IntegerType(first.integer - second.asNumber(spiralContext, pipelineContext).toInt())
                is VariableValue.DecimalType -> VariableValue.DecimalType(first.decimal - second.asNumber(spiralContext, pipelineContext).toDouble())
//                is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        object DIVIDE : ExpressionOperation() {
            override suspend fun _operate(spiralContext: SpiralContext, pipelineContext: PipelineContext, first: VariableValue, second: VariableValue): VariableValue = when (first) {
                is VariableValue.StringComponents -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> str.take(str.length / second.asNumber(spiralContext, pipelineContext).toInt().coerceAtLeast(1)) })
                    is VariableValue.StringType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> str.take(str.length / second.asNumber(spiralContext, pipelineContext).toInt().coerceAtLeast(1)) })
                    is VariableValue.BooleanType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).takeIf(second.boolean)
                            ?: "")
                    is VariableValue.IntegerType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> str.take(str.length / second.integer) })
                    is VariableValue.DecimalType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> str.take(if (second.decimal < 1.0) (str.length * second.decimal).toInt() else str.length / second.decimal.toInt()) })
//                    is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.StringType -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(first.string.take(first.string.length / second.asNumber(spiralContext, pipelineContext).toInt().coerceAtLeast(1)))
                    is VariableValue.StringType -> VariableValue.StringType(first.string.take(first.string.length / second.asNumber(spiralContext, pipelineContext).toInt().coerceAtLeast(1)))
                    is VariableValue.BooleanType -> VariableValue.StringType(first.string.takeIf(second.boolean) ?: "")
                    is VariableValue.IntegerType -> VariableValue.StringType(first.string.take(first.string.length / second.integer))
                    is VariableValue.DecimalType -> VariableValue.StringType(first.string.take(if (second.decimal < 1.0) (first.string.length * second.decimal).toInt() else first.string.length / second.decimal.toInt()))
//                    is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.BooleanType -> VariableValue.BooleanType(first.boolean.and(second.asNumber(spiralContext, pipelineContext).toInt() != 0))
                is VariableValue.IntegerType -> VariableValue.IntegerType(first.integer / second.asNumber(spiralContext, pipelineContext).toInt())
                is VariableValue.DecimalType -> VariableValue.DecimalType(first.decimal / second.asNumber(spiralContext, pipelineContext).toDouble())
//                is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        object MULTIPLY : ExpressionOperation() {
            override suspend fun _operate(spiralContext: SpiralContext, pipelineContext: PipelineContext, first: VariableValue, second: VariableValue): VariableValue = when (first) {
                is VariableValue.StringComponents -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> buildString { repeat(second.asNumber(spiralContext, pipelineContext).toInt()) { append(str) } } })
                    is VariableValue.StringType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> buildString { repeat(second.asNumber(spiralContext, pipelineContext).toInt()) { append(str) } } })
                    is VariableValue.BooleanType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).takeIf(second.boolean)
                            ?: "")
                    is VariableValue.IntegerType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> buildString { repeat(second.integer) { append(str) } } })
                    is VariableValue.DecimalType -> VariableValue.StringType(first.asString(spiralContext, pipelineContext).let { str -> buildString { repeat(second.decimal.toInt()) { append(str) } } })
//                    is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.StringType -> when (second) {
                    is VariableValue.StringComponents -> VariableValue.StringType(buildString { repeat(second.asNumber(spiralContext, pipelineContext).toInt()) { append(first.string) } })
                    is VariableValue.StringType -> VariableValue.StringType(buildString { repeat(second.asNumber(spiralContext, pipelineContext).toInt()) { append(first.string) } })
                    is VariableValue.BooleanType -> VariableValue.StringType(first.string.takeIf(second.boolean) ?: "")
                    is VariableValue.IntegerType -> VariableValue.StringType(buildString { repeat(second.integer) { append(first.string) } })
                    is VariableValue.DecimalType -> VariableValue.StringType(buildString { repeat(second.decimal.toInt()) { append(first.string) } })
//                    is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, first, pipelineContext[second.variableName]
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeFunction(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
//                    is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, first, pipelineContext.invokeScript(spiralContext, second.name, second.parameters)
//                            ?: VariableValue.NullType)
                    VariableValue.NullType -> first
                    else -> error("Non-flat $second")
                }
                is VariableValue.BooleanType -> VariableValue.BooleanType(first.boolean.and(second.asNumber(spiralContext, pipelineContext).toInt() != 0))
                is VariableValue.IntegerType -> VariableValue.IntegerType(first.integer * second.asNumber(spiralContext, pipelineContext).toInt())
                is VariableValue.DecimalType -> VariableValue.DecimalType(first.decimal * second.asNumber(spiralContext, pipelineContext).toDouble())
//                is VariableValue.VariableReferenceType -> _operate(spiralContext, pipelineContext, pipelineContext[first.variableName]
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedFunctionCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeFunction(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
//                is VariableValue.WrappedScriptCallType -> _operate(spiralContext, pipelineContext, pipelineContext.invokeScript(spiralContext, first.name, first.parameters)
//                        ?: VariableValue.NullType, second)
                VariableValue.NullType -> second
                else -> error("Non-flat $first")
            }
        }

        abstract suspend fun _operate(spiralContext: SpiralContext, pipelineContext: PipelineContext, first: VariableValue, second: VariableValue): VariableValue
        suspend fun operate(spiralContext: SpiralContext, pipelineContext: PipelineContext, first: VariableValue, second: VariableValue): VariableValue = _operate(spiralContext, pipelineContext, first.flatten(spiralContext, pipelineContext), second.flatten(spiralContext, pipelineContext))
    }

    data class ScopeType(val lines: Array<PipelineUnion>) : PipelineUnion()
    data class FunctionParameterType(val name: String?, val parameter: VariableValue) : PipelineUnion()
    data class ScriptParameterType(val name: String?, val parameter: VariableValue) : PipelineUnion()
    data class ReturnStatement(val value: VariableValue) : PipelineUnion()

    data class FunctionCallAction(val name: String, val parameters: Array<FunctionParameterType>) : PipelineUnion(), Action {
        override suspend fun run(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
            pipelineContext.invokeFunction(spiralContext, name, parameters)
        }
    }

    data class ScriptCallAction(val name: String, val parameters: Array<ScriptParameterType>) : PipelineUnion(), Action {
        override suspend fun run(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
            pipelineContext.invokeScript(spiralContext, name, parameters)
        }
    }

    data class AssignVariableAction(val variableName: String, val variableValue: VariableValue, val global: Boolean = false) : PipelineUnion(), Action {
        override suspend fun run(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
            pipelineContext[variableName, global] = variableValue
        }
    }

    data class FunctionDeclaration(val functionName: String, val parameterNames: Array<String>, val global: Boolean = false, val body: ScopeType?) : PipelineUnion(), Action {
        fun asPipelineFunction(): PipelineFunction<VariableValue?> = PipelineFunction(functionName, *Array(parameterNames.size) { Pair(parameterNames[it], null) }, func = this::invoke)
        suspend operator fun invoke(spiralContext: SpiralContext, pipelineContext: PipelineContext, parameters: Map<String, Any?>): VariableValue? =
                body?.run(spiralContext, pipelineContext, parameters)

        override suspend fun run(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
            pipelineContext.register(functionName, asPipelineFunction(), global)
        }
    }
}

suspend fun PipelineUnion.VariableValue.flattenIfPresent(spiralContext: SpiralContext, pipelineContext: PipelineContext): PipelineUnion.VariableValue?
        = flatten(spiralContext, pipelineContext).takeIfPresent()

suspend fun PipelineUnion.VariableValue.asFlattenedStringIfPresent(spiralContext: SpiralContext, pipelineContext: PipelineContext): String? =
        flatten(spiralContext, pipelineContext).takeIfPresent()?.asString(spiralContext, pipelineContext)

@ExperimentalUnsignedTypes
fun <T: PipelineUnion.VariableValue> T.takeIfPresent(): T? = when (this) {
    is PipelineUnion.VariableValue.NullType -> null
    else -> this
}