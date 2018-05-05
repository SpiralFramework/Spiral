package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("param")
data class ColladaParamPojo(
        @JacksonXmlProperty(isAttribute = true)
        val name: String?,
        @JacksonXmlProperty(isAttribute = true)
        val sid: String?,
        @JacksonXmlProperty(isAttribute = true)
        val type: String,
        @JacksonXmlProperty(isAttribute = true)
        val semantic: String?
)