package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("effect")
class ColladaEffectPojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String,
        @JacksonXmlProperty(isAttribute = true)
        val name: String? = null,

        val profile_COMMON: List<ColladaProfileCommonPojo>? = null
)