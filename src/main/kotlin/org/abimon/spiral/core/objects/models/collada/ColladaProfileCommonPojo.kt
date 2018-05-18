package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("profile_COMMON")
data class ColladaProfileCommonPojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String? = null,

        val image: List<ColladaImagePojo>? = null,
        val newparam: List<ColladaNewParamPojo>? = null,

        val technique: ColladaTechniqueFxPojo
)