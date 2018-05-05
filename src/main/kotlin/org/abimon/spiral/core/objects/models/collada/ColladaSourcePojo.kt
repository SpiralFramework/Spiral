package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("source")
data class ColladaSourcePojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String,
        @JacksonXmlProperty(isAttribute = true)
        val name: String?,

        val float_array: ColladaFloatArrayPojo?,
        val technique_common: ColladaTechniqueCommonPojo?
)