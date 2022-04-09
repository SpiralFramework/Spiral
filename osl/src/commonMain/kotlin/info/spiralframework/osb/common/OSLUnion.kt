package info.spiralframework.osb.common

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public sealed class OSLUnion {
    public abstract class NumberType : OSLUnion() {
        public abstract val number: Number
        public inline operator fun <T> invoke(operation: Number.() -> T): T = number.operation()
    }

    public data class Int8NumberType(override val number: Number) : NumberType()
    public data class Int16LENumberType(override val number: Number) : NumberType()
    public data class Int16BENumberType(override val number: Number) : NumberType()
    public data class Int24LENumberType(override val number: Number) : NumberType()
    public data class Int24BENumberType(override val number: Number) : NumberType()
    public data class Int32LENumberType(override val number: Number) : NumberType()
    public data class Int32BENumberType(override val number: Number) : NumberType()
    public data class IntegerNumberType(override val number: Number) : NumberType()
    public data class DecimalNumberType(override val number: Number) : NumberType()

    public abstract class StringType(public open val string: String) : OSLUnion(), CharSequence by string {
        public inline operator fun <T> invoke(operation: String.() -> T): T = string.operation()
    }

    public data class RawStringType(override val string: String) : StringType(string)
    public data class LabelType(val label: String) : StringType(label)
    public data class ParameterType(val parameter: String) : StringType(parameter)

    public data class LongReferenceType(val longReference: ByteArray) : OSLUnion() {
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

    public data class VariableReferenceType(val variableName: String) : OSLUnion()
    public data class LongLabelType(val longReference: ByteArray) : OSLUnion() {
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

    public data class LongParameterType(val longReference: ByteArray) : OSLUnion() {
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

    public data class ActionType(val actionName: ByteArray) : OSLUnion() {
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

    public data class FunctionParameterType(val parameterName: String?, val parameterValue: OSLUnion) : OSLUnion()
    public data class FunctionCallType(val functionName: String, val parameters: Array<FunctionParameterType>) :
        OSLUnion() {
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

    public data class BooleanType(val boolean: Boolean) : OSLUnion() {
        public inline operator fun <T> invoke(operation: Boolean.() -> T): T = boolean.operation()
    }

    public object UndefinedType : OSLUnion()
    public object NullType : OSLUnion()
    public object NoOpType : OSLUnion()

    public fun orElse(other: OSLUnion): OSLUnion =
        if (this is NullType || this is UndefinedType || this is NoOpType) other
        else this
}

public inline fun runNoOp(block: () -> Unit): OSLUnion.NoOpType {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    block()
    return OSLUnion.NoOpType
}