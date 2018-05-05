package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("input")
data class ColladaInputSharedPojo(
        @JacksonXmlProperty(isAttribute = true)
        val semantic: String,
        @JacksonXmlProperty(isAttribute = true)
        val source: String,
        @JacksonXmlProperty(isAttribute = true)
        val offset: Int,
        @JacksonXmlProperty(isAttribute = true)
        val set: Int? = null
)