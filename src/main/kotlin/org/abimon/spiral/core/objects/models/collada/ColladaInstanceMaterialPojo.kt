package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("instance_material")
data class ColladaInstanceMaterialPojo(
        @JacksonXmlProperty(isAttribute = true)
        val sid: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val name: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val target: String,
        @JacksonXmlProperty(isAttribute = true)
        val symbol: String
)