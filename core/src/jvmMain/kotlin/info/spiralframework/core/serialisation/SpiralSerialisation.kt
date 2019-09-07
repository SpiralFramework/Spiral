package info.spiralframework.core.serialisation

import com.fasterxml.jackson.databind.ObjectMapper

interface SpiralSerialisation {
    object NoOp: SpiralSerialisation {
        override val jsonMapper: ObjectMapper
            get() = throw IllegalStateException("Serialisation NoOp")
        override val yamlMapper: ObjectMapper
            get() = throw IllegalStateException("Serialisation NoOp")
        override val xmlMapper: ObjectMapper
            get() = throw IllegalStateException("Serialisation NoOp")
    }
    val jsonMapper: ObjectMapper
    val yamlMapper: ObjectMapper
    val xmlMapper: ObjectMapper
}