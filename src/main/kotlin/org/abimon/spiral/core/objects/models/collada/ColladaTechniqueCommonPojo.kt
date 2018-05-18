package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("technique_common")
data class ColladaTechniqueCommonPojo(
        val accessor: ColladaAccessorPojo? = null,
        val instance_material: List<ColladaInstanceMaterialPojo>? = null
) {
    companion object {
        fun vertexAccessorFor(count: Int, source: String): ColladaTechniqueCommonPojo {
            return ColladaTechniqueCommonPojo(ColladaAccessorPojo(count, stride = 3, param = listOf(ColladaParamPojo.X, ColladaParamPojo.Y, ColladaParamPojo.Z), source = source))
        }

        fun uvAccessorFor(count: Int, source: String): ColladaTechniqueCommonPojo {
            return ColladaTechniqueCommonPojo(ColladaAccessorPojo(count, stride = 2, param = listOf(ColladaParamPojo.U, ColladaParamPojo.V), source = source))
        }
    }
}