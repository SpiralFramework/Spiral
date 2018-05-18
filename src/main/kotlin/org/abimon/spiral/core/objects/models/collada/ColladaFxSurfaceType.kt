package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonProperty

enum class ColladaFxSurfaceType {
    UNTYPED,
    @JsonProperty("1D")
    ONE_D,
    @JsonProperty("2D")
    TWO_D,
    @JsonProperty("3D")
    THREE_D,
    CUBE,
    DEPTH,
    RECT
}