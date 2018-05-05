package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("accessor")
data class ColladaAccessorPojo(
        @JacksonXmlProperty(isAttribute = true)
        val count: Int,
        @JacksonXmlProperty(isAttribute = true)
        val offset: Int = 0,
        @JacksonXmlProperty(isAttribute = true)
        val source: String,
        @JacksonXmlProperty(isAttribute = true)
        val stride: Int = 1,

        val param: List<ColladaParamPojo> = emptyList()
)