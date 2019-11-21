package info.spiralframework.osl

import info.spiralframework.formats.scripting.CustomLin
import info.spiralframework.formats.scripting.CustomWordScript
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.wrd.WrdScript

sealed class OSLUnion {
    data class NumberType(val number: Number): OSLUnion() {
        override fun represent(): String = number.toString()

        inline operator fun <T> invoke(operation: Number.() -> T): T = number.operation()
    }
    abstract class StringType(open val string: String): OSLUnion(), CharSequence by string {
        override fun represent(): String = string

        inline operator fun <T> invoke(operation: String.() -> T): T = string.operation()
    }
    data class RawStringType(override val string: String): StringType(string)
    data class LabelType(val label: String): StringType(label)
    data class ParameterType(val parameter: String): StringType(parameter)

    data class BooleanType(val boolean: Boolean): OSLUnion() {
        override fun represent(): String = boolean.toString()

        inline operator fun <T> invoke(operation: Boolean.() -> T): T = boolean.operation()
    }

    data class LinEntryType(val entry: LinEntry): OSLUnion() {
        override fun represent(): String = entry.format()

        inline operator fun <T> invoke(operation: LinEntry.() -> T): T = entry.operation()
    }
    data class CustomLinType(val lin: CustomLin): OSLUnion() {
        override fun represent(): String = buildString {
            appendln(lin.entries.joinToString("\n", transform = LinEntry::format))
            lin.linesOfText.forEachIndexed { index, str -> appendln("${index shr 8},${index and 0xFF}: $str") }
        }

        inline operator fun <T> invoke(operation: CustomLin.() -> T): T = lin.operation()
    }

    data class WrdEntryType(val entry: WrdScript): OSLUnion() {
        override fun represent(): String = "0x${entry.opCode.toString(16)}|${entry.rawArguments.joinToString()}"

        inline operator fun <T> invoke(operation: WrdScript.() -> T): T = entry.operation()
    }
    data class CustomWrdType(val wrd: CustomWordScript): OSLUnion() {
        override fun represent(): String = buildString {
            appendln(wrd.entries.joinToString("\n") { entry -> "0x${entry.opCode.toString(16)}|${entry.rawArguments.joinToString()}" })
            wrd.labels.forEachIndexed { index, str -> appendln("[Label] ${index shr 8},${index and 0xFF}: $str") }
            wrd.parameters.forEachIndexed { index, str -> appendln("[Param] ${index shr 8},${index and 0xFF}: $str") }
            wrd.strings.forEachIndexed { index, str -> appendln("[String] ${index shr 8},${index and 0xFF}: $str") }
        }
    }

    object UndefinedType: OSLUnion() {
        override fun represent(): String = "[undefined]"
    }
    object NullType: OSLUnion() {
        override fun represent(): String = "[null]"
    }

    object NoOpType: OSLUnion() {
        override fun represent(): String = "[no-op]"
    }

    abstract fun represent(): String
}