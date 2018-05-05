package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("COLLADA")
data class ColladaPojo(
        //val asset: ColladaAssetPojo,
        val library_geometries: ColladaLibraryGeometriesPojo,
        val library_visual_scenes: ColladaLibraryVisualScenesPojo?,
        val scene: ColladaScenePojo?
)