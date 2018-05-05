package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("mesh")
data class ColladaMeshPojo(
        val source: List<ColladaSourcePojo>,
        val vertices: ColladaVerticesPojo,
        val triangles: List<ColladaTrianglesPojo> = emptyList()
)