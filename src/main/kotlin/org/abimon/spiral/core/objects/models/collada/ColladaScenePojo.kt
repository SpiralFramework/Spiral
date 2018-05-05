package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("scene")
data class ColladaScenePojo(
        val instance_visual_scene: ColladaInstanceVisualScenePojo?
)