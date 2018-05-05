package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("library_visual_scenes")
data class ColladaLibraryVisualScenesPojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String?,
        @JacksonXmlProperty(isAttribute = true)
        val name: String?,

        val visual_scene: List<ColladaVisualScenePojo>
)