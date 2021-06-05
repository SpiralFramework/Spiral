package info.spiralframework.core.serialisation

import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

//import com.fasterxml.jackson.databind.ObjectMapper

interface SpiralSerialisation {
    object NoOp: SpiralSerialisation {
//        override val jsonMapper: ObjectMapper
//            get() = throw IllegalStateException("Serialisation NoOp")
//        override val yamlMapper: ObjectMapper
//            get() = throw IllegalStateException("Serialisation NoOp")
//        override val xmlMapper: ObjectMapper
//            get() = throw IllegalStateException("Serialisation NoOp")

        override val json: StringFormat = Json
    }
/*    val jsonMapper: ObjectMapper
    val yamlMapper: ObjectMapper
    val xmlMapper: ObjectMapper*/

    val json: StringFormat
}