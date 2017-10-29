package org.abimon.spiral.core.objects.models

data class SRDIMesh(
        val vertices: List<Triple<Float, Float, Float>>,
        val uvs: List<Pair<Float, Float>>,
        val faces: List<Triple<Int, Int, Int>>
)