package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("library_geometries")
data class ColladaLibraryGeometriesPojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String?,
        @JacksonXmlProperty(isAttribute = true)
        val name: String?,

        val geometry: List<ColladaGeometryPojo>
)