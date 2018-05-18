package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("technique")
data class ColladaTechniqueFxPojo(
        @JacksonXmlProperty(isAttribute = true)
        val sid: String,
        @JacksonXmlProperty(isAttribute = true)
        val id: String? = null,

        val phong: ColladaPhongPojo
)