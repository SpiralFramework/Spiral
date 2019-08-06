package info.spiralframework.core.serialisation

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import info.spiralframework.base.util.SemVer

object SemVerSerialisation {
    object SERIALISER : JsonSerializer<SemVer>() {
        override fun serialize(value: SemVer, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString("${value.major}.${value.minor}.${value.patch}")
        }

        override fun handledType(): Class<SemVer> = SemVer::class.java
    }

    object DESERIALISER : JsonDeserializer<SemVer>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SemVer {
            val components = p.valueAsString
                    ?.split('.')
                    ?.mapNotNull(String::toIntOrNull)
                    ?: emptyList()
            return SemVer(
                    components.elementAtOrNull(0) ?: 0,
                    components.elementAtOrNull(1) ?: 0,
                    components.elementAtOrNull(2) ?: 0
            )
        }

        override fun handledType(): Class<SemVer> = SemVer::class.java
    }

    class MODULE : SimpleModule("Semantic Version Serialisation", Version.unknownVersion(), mapOf(SemVer::class.java to DESERIALISER), listOf(SERIALISER))
}