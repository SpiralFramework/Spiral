package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("library_materials")
data class ColladaLibraryMaterialsPojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String? = null,
        @JacksonXmlProperty(isAttribute = true)
        val name: String? = null,

        val material: List<ColladaMaterialPojo>
)