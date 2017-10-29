package org.abimon.spiral.core.objects.models

import org.abimon.spiral.util.OBJParser
import org.abimon.spiral.util.TriFace
import org.abimon.spiral.util.UV
import org.abimon.spiral.util.Vertex
import org.abimon.visi.io.DataSource
import java.io.InputStreamReader

class OBJModel(val dataSource: DataSource) {
    val vertices: List<Vertex>
    val uvs: List<UV>
    val faces: List<TriFace>

    init {
        val vertList: MutableList<Vertex> = ArrayList()
        val uvList: MutableList<UV> = ArrayList()
        val faceList: MutableList<TriFace> = ArrayList()

        OBJParser.runner.run(InputStreamReader(dataSource.inputStream).use { it.readText() }).valueStack.forEach { value ->
            if (value is List<*>) {
                when(value[0]) {
                    OBJParser.VERTEX_ID -> vertList.add(OBJParser.toVertex(value.subList(1, value.size)))
                    OBJParser.UV_ID -> uvList.add(OBJParser.toUV(value.subList(1, value.size)))
                    OBJParser.FACE_ID -> faceList.add(OBJParser.toFace(value.subList(1, value.size)))
                }
            }
        }

        vertices = vertList
        uvs = uvList
        faces = faceList
    }
}