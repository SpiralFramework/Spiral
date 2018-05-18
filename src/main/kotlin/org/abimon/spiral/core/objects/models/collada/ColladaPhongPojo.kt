package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("phong")
data class ColladaPhongPojo(
        val emission: ColladaCommonColorOrTextureTypePojo? = null,
        val ambient: ColladaCommonColorOrTextureTypePojo? = null,
        val diffuse: ColladaCommonFloatOrParamTypePojo? = null,
        val specular: ColladaCommonColorOrTextureTypePojo? = null,
        val shininess: ColladaCommonFloatOrParamTypePojo? = null,
        val reflective: ColladaCommonColorOrTextureTypePojo? = null,
        val reflectivity: ColladaCommonFloatOrParamTypePojo? = null,
        val transparent: ColladaCommonColorOrTextureTypePojo? = null,
        val transparency: ColladaCommonFloatOrParamTypePojo? = null,
        val index_of_refraction: ColladaCommonFloatOrParamTypePojo? = null
)
