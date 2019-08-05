package info.spiralframework.json

sealed class JsonType {
    data class JsonObject(val map: Map<String, JsonType>) : JsonType(), Map<String, JsonType> by map {
        override fun print(): String = map.entries.joinToString(prefix = "{", postfix = "}") { (k, v) -> "\"$k\":${v.print()}" }

        @Suppress("IMPLICIT_CAST_TO_ANY")
        fun toMap(): Map<String, Any?> =
                map.mapValues { (_, value) ->
                    when (value) {
                        is JsonType.JsonObject -> value.toMap()
                        is JsonType.JsonArray -> value.toList()
                        is JsonType.JsonString -> value.string
                        is JsonType.JsonNumber -> value.number
                        is JsonType.JsonBoolean -> value.boolean
                        JsonType.JsonNull -> null
                    }
                }
    }

    data class JsonArray(val list: List<JsonType>) : JsonType(), List<JsonType> by list {
        override fun print(): String = list.joinToString(prefix = "[", postfix = "]", transform = JsonType::print)

        @Suppress("IMPLICIT_CAST_TO_ANY")
        fun toList(): List<Any?> =
                list.map { value ->
                    when (value) {
                        is JsonObject -> value.toMap()
                        is JsonArray -> value.toList()
                        is JsonString -> value.string
                        is JsonNumber -> value.number
                        is JsonBoolean -> value.boolean
                        JsonNull -> null
                    }
                }
    }

    data class JsonString(val string: String) : JsonType() {
        override fun print(): String = "\"$string\""
    }

    data class JsonNumber(val number: Number) : JsonType() {
        override fun print(): String = number.toString()
    }

    data class JsonBoolean(val boolean: Boolean) : JsonType() {
        override fun print(): String = boolean.toString()
    }

    object JsonNull : JsonType() {
        override fun print(): String = "null"
    }

    abstract fun print(): String
}