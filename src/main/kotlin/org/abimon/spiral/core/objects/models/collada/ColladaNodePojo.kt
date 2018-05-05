package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("node")
data class ColladaNodePojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val name: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val sid: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val type: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val layer: String? = null,

        val instance_geometry: List<ColladaInstanceGeometryPojo> = emptyList()
)