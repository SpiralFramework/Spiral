package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("image")
data class ColladaImagePojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val name: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val format: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val height: Int? = null,
        @JacksonXmlProperty(isAttribute = true)
        val width: Int? = null,
        @JacksonXmlProperty(isAttribute = true)
        val depth: Int = 1,

        val init_from: ColladaInitFromPojo
)