package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("COLLADA")
data class ColladaPojo(
        @JacksonXmlProperty(isAttribute = true)
        val version: String = "1.4.1",
        @JacksonXmlProperty(isAttribute = true)
        val xmlns: String = "http://www.collada.org/2005/11/COLLADASchema",

        val asset: ColladaAssetPojo,
        val library_geometries: ColladaLibraryGeometriesPojo,
        val library_visual_scenes: ColladaLibraryVisualScenesPojo?,
        val scene: ColladaScenePojo?
)