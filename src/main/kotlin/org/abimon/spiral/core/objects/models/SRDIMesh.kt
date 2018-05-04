package org.abimon.spiral.core.objects.models

import org.abimon.spiral.core.utils.TriFace
import org.abimon.spiral.core.utils.UV
import org.abimon.spiral.core.utils.Vertex
import java.util.*

open class SRDIMesh(val vertices: Array<Vertex>, val uvs: Array<UV>, val faces: Array<TriFace>) {
    var name: String? = null
    var normals: Array<Vertex>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SRDIMesh

        if (!Arrays.equals(vertices, other.vertices)) return false
        if (!Arrays.equals(uvs, other.uvs)) return false
        if (!Arrays.equals(faces, other.faces)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(vertices)
        result = 31 * result + Arrays.hashCode(uvs)
        result = 31 * result + Arrays.hashCode(faces)
        return result
    }

    operator fun component1(): Array<Vertex> = vertices
    operator fun component2(): Array<UV> = uvs
    operator fun component3(): Array<TriFace> = faces
}