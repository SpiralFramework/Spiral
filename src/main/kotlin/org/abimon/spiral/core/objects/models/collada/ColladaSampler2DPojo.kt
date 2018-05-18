package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("sampler2D")
data class ColladaSampler2DPojo(
        val source: String
)