package info.spiralframework.formats.common.scripting.wrd

import info.spiralframework.formats.common.data.EnumWordScriptCommand

sealed class WordScriptValue(open val raw: Int) {
    companion object {
        fun parse(raw: ByteArray, labels: Array<String>, parameters: Array<String>, text: Array<String>?, types: ((Int) -> EnumWordScriptCommand)?): Array<WordScriptValue> =
                Array(raw.size / 2) { invoke((raw[it * 2].toInt() and 0xFF shl 8) or (raw[it * 2 + 1].toInt() and 0xFF), labels, parameters, text, types?.invoke(it)) }

        fun parse(raw: IntArray, labels: Array<String>, parameters: Array<String>, text: Array<String>?, types: ((Int) -> EnumWordScriptCommand)?): Array<WordScriptValue> =
                Array(raw.size) { invoke(raw[it], labels, parameters, text, types?.invoke(it)) }

        fun parse(raw: List<Int>, labels: Array<String>, parameters: Array<String>, text: Array<String>?, types: ((Int) -> EnumWordScriptCommand)?): Array<WordScriptValue> =
                Array(raw.size) { invoke(raw[it], labels, parameters, text, types?.invoke(it)) }

        operator fun invoke(raw: Int, labels: Array<String>, parameters: Array<String>, text: Array<String>?, type: EnumWordScriptCommand?): WordScriptValue {
            return when (type) {
                EnumWordScriptCommand.LABEL -> Label(labels[raw], raw)
                EnumWordScriptCommand.PARAMETER -> Parameter(parameters[raw], raw)
                EnumWordScriptCommand.TEXT -> if (text != null) InternalText(text[raw], raw) else ExternalText(raw)
                EnumWordScriptCommand.RAW -> Raw(raw)
                null -> Unknown(raw)
            }
        }

        fun parse(raw: ByteArray, labels: List<String>, parameters: List<String>, text: List<String>?, types: ((Int) -> EnumWordScriptCommand)?): Array<WordScriptValue> =
                Array(raw.size / 2) { invoke((raw[it * 2].toInt() and 0xFF shl 8) or (raw[it * 2 + 1].toInt() and 0xFF), labels, parameters, text, types?.invoke(it)) }

        fun parse(raw: IntArray, labels: List<String>, parameters: List<String>, text: List<String>?, types: ((Int) -> EnumWordScriptCommand)?): Array<WordScriptValue> =
                Array(raw.size) { invoke(raw[it], labels, parameters, text, types?.invoke(it)) }

        fun parse(raw: List<Int>, labels: List<String>, parameters: List<String>, text: List<String>?, types: ((Int) -> EnumWordScriptCommand)?): Array<WordScriptValue> =
                Array(raw.size) { invoke(raw[it], labels, parameters, text, types?.invoke(it)) }

        operator fun invoke(raw: Int, labels: List<String>, parameters: List<String>, text: List<String>?, type: EnumWordScriptCommand?): WordScriptValue {
            return when (type) {
                EnumWordScriptCommand.LABEL -> Label(labels[raw], raw)
                EnumWordScriptCommand.PARAMETER -> Parameter(parameters[raw], raw)
                EnumWordScriptCommand.TEXT -> if (text != null) InternalText(text[raw], raw) else ExternalText(raw)
                EnumWordScriptCommand.RAW -> Raw(raw)
                null -> Unknown(raw)
            }
        }
    }

    data class Label(val label: String, override val raw: Int) : WordScriptValue(raw) {
        override fun toString(): String = "@{$label}"
    }

    data class Parameter(val param: String, override val raw: Int) : WordScriptValue(raw) {
        override fun toString(): String = "%{$param}"
    }

    abstract class Text(raw: Int): WordScriptValue(raw)

    data class InternalText(val text: String, override val raw: Int) : Text(raw) {
        override fun toString(): String = "\"$text\""
    }

    data class ExternalText(override val raw: Int) : Text(raw) {
        override fun toString(): String = raw.toString()
    }

    data class Raw(override val raw: Int) : WordScriptValue(raw) {
        override fun toString(): String = raw.toString()
    }

    data class Unknown(override val raw: Int) : WordScriptValue(raw) {
        override fun toString(): String = raw.toString()
    }
}