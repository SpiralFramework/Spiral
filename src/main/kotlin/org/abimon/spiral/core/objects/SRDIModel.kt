package org.abimon.spiral.core.objects

import org.abimon.spiral.util.toFloat
import org.abimon.spiral.util.toShort
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.and

class SRDIModel(data: DataSource) {
    companion object {
        val sequence = byteArrayOf(0, 0, 2, 0, 1, 0)
    }

    val meshes: MutableList<SRDIMesh> = ArrayList()
    
    init {
        data.use { stream ->
            var doingFaces = false

            val vertices: MutableList<Triple<Float, Float, Float>> = ArrayList()
            val uvs: MutableList<Pair<Float, Float>> = ArrayList()
            val faces: MutableList<Triple<Int, Int, Int>> = ArrayList()

            stream@while(stream.available() >= 48) {
                val buffer = ByteArray(48).apply { stream.read(this) }

//                if(!doingFaces && (toShort(buffer, true, true, 0) < 3 || toShort(buffer, true, true, 2) < 3 || toShort(buffer, true, true, 4) < 3))
//                    doingFaces = true

                if(!doingFaces && buffer.copyOfRange(0, 6) contentEquals sequence)
                    doingFaces = true

//                if(doingFaces && toInt(buffer, true, true, 24) == 0 && toInt(buffer, true, true, 28) == 1065353216)
//                    break

                if(doingFaces && (toShort(buffer, true, true, 0) !in uvs.indices || toShort(buffer, true, true, 2) !in uvs.indices || toShort(buffer, true, true, 4) !in uvs.indices)) {
                    //meshes.add(SRDIMesh(vertices.toList(), uvs.toList(), faces.toList()))
//                    vertices.clear()
//                    uvs.clear()
//                    faces.clear()
//
//                    doingFaces = false

                    break@stream
                }

                if(!doingFaces) {
                    val x = toFloat(buffer, true, 0)
                    val y = toFloat(buffer, true, 4)
                    val z = toFloat(buffer, true, 8)

                    vertices.add(x to y and z)

                    val u = toFloat(buffer, true, 24)
                    val v = toFloat(buffer, true, 28)

                    uvs.add(u to v)
                } else {
                    for (i in 0 until buffer.size / 3 / 2) {
                        val a = toShort(buffer, true, true, i * 6 + 0)
                        val b = toShort(buffer, true, true, i * 6 + 2)
                        val c = toShort(buffer, true, true, i * 6 + 4)

                        faces.add(a to b and c)
                    }
                }
            }

            meshes.add(SRDIMesh(vertices.toList(), uvs.toList(), faces.toList()))
        }
    }
}