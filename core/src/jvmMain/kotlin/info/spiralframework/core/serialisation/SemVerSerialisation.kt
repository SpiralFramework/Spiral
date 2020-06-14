package info.spiralframework.core.serialisation

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.kornea.toolkit.common.SemanticVersion

object SemanticVersionSerialisation {
    object SERIALISER : JsonSerializer<SemanticVersion>() {
        override fun serialize(value: SemanticVersion, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString("${value.major}.${value.minor}.${value.patch}")
        }

        override fun handledType(): Class<SemanticVersion> = SemanticVersion::class.java
    }

    object DESERIALISER : JsonDeserializer<SemanticVersion>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SemanticVersion {
            val components = p.valueAsString
                    ?.split('.')
                    ?.mapNotNull(String::toIntOrNull)
                    ?: emptyList()
            return SemanticVersion(
                    components.elementAtOrNull(0) ?: 0,
                    components.elementAtOrNull(1) ?: 0,
                    components.elementAtOrNull(2) ?: 0
            )
        }

        override fun handledType(): Class<SemanticVersion> = SemanticVersion::class.java
    }

    class MODULE : SimpleModule("Semantic Version Serialisation", Version.unknownVersion(), mapOf(SemanticVersion::class.java to DESERIALISER), listOf(SERIALISER))
}