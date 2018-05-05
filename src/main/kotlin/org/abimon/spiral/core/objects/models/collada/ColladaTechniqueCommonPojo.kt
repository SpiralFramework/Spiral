package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("technique_common")
data class ColladaTechniqueCommonPojo(
        val accessor: ColladaAccessorPojo
)