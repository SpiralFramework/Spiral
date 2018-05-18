package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("material")
data class ColladaMaterialPojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val name: String? = null,

        val instance_effect: ColladaInstanceEffectPojo
)