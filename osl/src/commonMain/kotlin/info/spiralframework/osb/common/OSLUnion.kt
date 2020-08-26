package info.spiralframework.osb.common

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class OSLUnion {
    abstract class NumberType : OSLUnion() {
        abstract val number: Number
        inline operator fun <T> invoke(operation: Number.() -> T): T = number.operation()
    }

    data class Int8NumberType(override val number: Number) : NumberType()
    data class Int16LENumberType(override val number: Number) : NumberType()
    data class Int16BENumberType(override val number: Number) : NumberType()
    data class Int24LENumberType(override val number: Number) : NumberType()
    data class Int24BENumberType(override val number: Number) : NumberType()
    data class Int32LENumberType(override val number: Number) : NumberType()
    data class Int32BENumberType(override val number: Number) : NumberType()
    data class IntegerNumberType(override val number: Number) : NumberType()
    data class DecimalNumberType(override val number: Number) : NumberType()

    abstract class StringType(open val string: String) : OSLUnion(), CharSequence by string {
        inline operator fun <T> invoke(operation: String.() -> T): T = string.operation()
    }

    data class RawStringType(override val string: String) : StringType(string)
    data class LabelType(val label: String) : StringType(label)
    data class ParameterType(val parameter: String) : StringType(parameter)

    data class LongReferenceType(val longReference: ByteArray) : OSLUnion() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as LongReferenceType

            if (!longReference.contentEquals(other.longReference)) return false

            return true
        }

        override fun hashCode(): Int {
            return longReference.contentHashCode()
        }
    }

    data class VariableReferenceType(val variableName: String) : OSLUnion()
    data class LongLabelType(val longReference: ByteArray) : OSLUnion() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as LongLabelType

            if (!longReference.contentEquals(other.longReference)) return false

            return true
        }

        override fun hashCode(): Int {
            return longReference.contentHashCode()
        }
    }

    data class LongParameterType(val longReference: ByteArray) : OSLUnion() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as LongLabelType

            if (!longReference.contentEquals(other.longReference)) return false

            return true
        }

        override fun hashCode(): Int {
            return longReference.contentHashCode()
        }
    }

    data class ActionType(val actionName: ByteArray) : OSLUnion() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ActionType

            if (!actionName.contentEquals(other.actionName)) return false

            return true
        }

        override fun hashCode(): Int {
            return actionName.contentHashCode()
        }
    }

    data class FunctionParameterType(val parameterName: String?, val parameterValue: OSLUnion) : OSLUnion()
    data class FunctionCallType(val functionName: String, val parameters: Array<FunctionParameterType>) : OSLUnion() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as FunctionCallType

            if (functionName != other.functionName) return false
            if (!parameters.contentEquals(other.parameters)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = functionName.hashCode()
            result = 31 * result + parameters.contentHashCode()
            return result
        }
    }

    data class BooleanType(val boolean: Boolean) : OSLUnion() {
        inline operator fun <T> invoke(operation: Boolean.() -> T): T = boolean.operation()
    }

    object UndefinedType : OSLUnion()
    object NullType : OSLUnion()
    object NoOpType : OSLUnion()

    fun orElse(other: OSLUnion): OSLUnion =
            if (this is NullType || this is UndefinedType || this is NoOpType) other
            else this
}

inline fun runNoOp(block: () -> Unit): OSLUnion.NoOpType {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return OSLUnion.NoOpType
}