package org.abimon.spiral.core.formats.models

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.models.OBJModel
import org.abimon.spiral.util.InputStreamFuncDataSource
import java.io.InputStream

object OBJModelFormat: SpiralFormat {
    override val name: String = "OBJ"
    override val extension: String? = "obj"
    override val conversions: Array<SpiralFormat> = emptyArray()

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean {
        val model = OBJModel(InputStreamFuncDataSource(dataSource))

        return (model.vertices.isNotEmpty() && model.faces.isNotEmpty()) || (model.uvs.isNotEmpty() && model.faces.isNotEmpty())
    }
}