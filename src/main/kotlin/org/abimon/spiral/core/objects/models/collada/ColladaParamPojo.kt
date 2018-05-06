package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("param")
data class ColladaParamPojo(
        @JacksonXmlProperty(isAttribute = true)
        val name: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val sid: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val type: String,
        @JacksonXmlProperty(isAttribute = true)
        val semantic: String? = null
) {
    companion object {
        val X = ColladaParamPojo(name = "X", type = "float")
        val Y = ColladaParamPojo(name = "Y", type = "float")
        val Z = ColladaParamPojo(name = "Z", type = "float")

        val U = ColladaParamPojo(name = "S", type = "float")
        val V = ColladaParamPojo(name = "T", type = "float")
    }
}