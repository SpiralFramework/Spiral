package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("geometry")
data class ColladaGeometryPojo (
    @JacksonXmlProperty(isAttribute = true)
    val id: String?,
    @JacksonXmlProperty(isAttribute = true)
    val name: String?,

    val mesh: ColladaMeshPojo
)