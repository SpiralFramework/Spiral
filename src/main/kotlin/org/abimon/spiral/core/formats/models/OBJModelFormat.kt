package org.abimon.spiral.core.formats.models

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.models.OBJModel
import org.abimon.visi.io.DataSource

object OBJModelFormat: SpiralFormat {
    override val name: String = "OBJ"
    override val extension: String? = "obj"
    override val conversions: Array<SpiralFormat> = emptyArray()

    override fun isFormat(source: DataSource): Boolean {
        val model = OBJModel(source)

        return (model.vertices.isNotEmpty() && model.faces.isNotEmpty()) || (model.uvs.isNotEmpty() && model.faces.isNotEmpty())
    }
}