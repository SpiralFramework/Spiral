package org.abimon.spiral.core.objects.models

import org.abimon.spiral.core.utils.TriFace
import org.abimon.spiral.core.utils.UV
import org.abimon.spiral.core.utils.Vertex
import java.util.*

open class SRDIMapMesh(vertices: Array<Vertex>, uvs: Array<UV>, faces: Array<TriFace>, val unknownOne: Array<ByteArray>): SRDIMesh(vertices, uvs, faces) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SRDIMapMesh) return false

        if (!Arrays.equals(vertices, other.vertices)) return false
        if (!Arrays.equals(uvs, other.uvs)) return false
        if (!Arrays.equals(unknownOne, other.unknownOne)) return false
        if (!Arrays.equals(faces, other.faces)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(vertices)
        result = 31 * result + Arrays.hashCode(uvs)
        result = 31 * result + Arrays.hashCode(unknownOne)
        result = 31 * result + Arrays.hashCode(faces)
        return result
    }

    operator fun component4(): Array<ByteArray> = unknownOne
}