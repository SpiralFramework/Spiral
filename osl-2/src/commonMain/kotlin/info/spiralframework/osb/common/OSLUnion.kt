package info.spiralframework.osb.common

import info.spiralframework.base.common.text.appendln
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.scripting.lin.CustomLinScript
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.wrd.CustomWordScript
import info.spiralframework.formats.common.scripting.wrd.WrdEntry

sealed class OSLUnion {
    data class NumberType(val number: Number): OSLUnion() {
        inline operator fun <T> invoke(operation: Number.() -> T): T = number.operation()
    }
    abstract class StringType(open val string: String): OSLUnion(), CharSequence by string {
        inline operator fun <T> invoke(operation: String.() -> T): T = string.operation()
    }
    data class RawStringType(override val string: String): StringType(string)
    data class LabelType(val label: String): StringType(label)
    data class ParameterType(val parameter: String): StringType(parameter)

    data class LongReferenceType(val longReference: ByteArray): OSLUnion() {
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
    data class VariableReferenceType(val variableName: String): OSLUnion()
    data class LongLabelType(val longReference: ByteArray): OSLUnion() {
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

    data class LongParameterType(val longReference: ByteArray): OSLUnion() {
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
    data class ActionType(val actionName: ByteArray): OSLUnion() {
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

    data class BooleanType(val boolean: Boolean): OSLUnion() {
        inline operator fun <T> invoke(operation: Boolean.() -> T): T = boolean.operation()
    }

    object UndefinedType: OSLUnion()
    object NullType: OSLUnion()
    object NoOpType: OSLUnion()
}