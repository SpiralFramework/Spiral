package info.spiralframework.core.serialisation

import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

//import com.fasterxml.jackson.annotation.JsonInclude
//import com.fasterxml.jackson.annotation.JsonSetter
//import com.fasterxml.jackson.annotation.Nulls
//import com.fasterxml.jackson.core.JsonGenerator
//import com.fasterxml.jackson.databind.DeserializationFeature
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.SerializationFeature
//import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
//import com.fasterxml.jackson.dataformat.xml.XmlMapper
//import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
//import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
//import com.fasterxml.jackson.module.kotlin.KotlinModule
//import com.fasterxml.jackson.module.kotlin.registerKotlinModule
//import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

class DefaultSpiralSerialisation: SpiralSerialisation {
    /** Jackson mapper for JSON data */
//    override val jsonMapper: ObjectMapper = ObjectMapper()
//            .registerModules(KotlinModule(), Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), SemanticVersionSerialisation.MODULE())
//            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
//            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
//
//    /** Jackson mapper for YAML data */
//    override val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory())
//            .registerModules(KotlinModule(), Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), SemanticVersionSerialisation.MODULE())
//            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
//            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
//
//    /** Jackson mapper for XML data */
//    override val xmlMapper: ObjectMapper = XmlMapper(JacksonXmlModule().apply { setDefaultUseWrapper(false) })
//            .registerModules(KotlinModule(), Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), InstantSerialisation.MODULE())
//            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//            .enable(SerializationFeature.INDENT_OUTPUT)
//            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
//            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
//            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY))

    override val json: StringFormat = Json
}