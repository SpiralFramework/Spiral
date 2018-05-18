package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("texture")
/**
 * Undocumented, the best kind of feature /s
 */
data class ColladaTexturePojo(
        @JacksonXmlProperty(isAttribute = true)
        val texture: String
)