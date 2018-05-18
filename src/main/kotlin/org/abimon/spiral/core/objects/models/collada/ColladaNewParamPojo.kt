package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("newparam")
data class ColladaNewParamPojo(
        @JacksonXmlProperty(isAttribute = true)
        val sid: String,

        val surface: ColladaSurfacePojo? = null,
        val sampler2D: ColladaSampler2DPojo? = null
)