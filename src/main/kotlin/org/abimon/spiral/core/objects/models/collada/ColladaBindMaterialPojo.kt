package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("bind_material")
data class ColladaBindMaterialPojo(
        val technique_common: ColladaTechniqueCommonPojo
) {
    companion object {
        fun bindMaterialFor(from: String, to: String): ColladaBindMaterialPojo = ColladaBindMaterialPojo(ColladaTechniqueCommonPojo(instance_material = listOf(ColladaInstanceMaterialPojo(target = "#$to", symbol = from))))
    }
}
