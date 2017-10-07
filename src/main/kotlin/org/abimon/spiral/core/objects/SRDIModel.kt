package org.abimon.spiral.core.objects

import com.sun.javafx.geom.Vec3f
import org.abimon.spiral.util.toFloat
import org.abimon.spiral.util.toInt
import org.abimon.spiral.util.toShort
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.and

class SRDIModel(data: DataSource) {
    companion object {
        val sequence = byteArrayOf(0, 0, 2, 0, 1, 0)
    }
    
    val vertices: MutableList<Vec3f> = ArrayList()
    val uvs: MutableList<Pair<Float, Float>> = ArrayList()
    val faces: MutableList<Triple<Int, Int, Int>> = ArrayList()
    
    init {
        data.use { stream ->
            var doingFaces = false
            
            while(stream.available() > 48) {
                val buffer = ByteArray(48).apply { stream.read(this) }

                if(!doingFaces && buffer.copyOfRange(0, 6) contentEquals sequence)
                    doingFaces = true

                if(doingFaces && toInt(buffer, true, true, 20) == 1065353216 && toInt(buffer, true, true, 44) == 1065353216)
                    break

                if(!doingFaces) {
                    val x = toFloat(buffer, true, 0)
                    val y = toFloat(buffer, true, 4)
                    val z = toFloat(buffer, true, 8)

                    vertices.add(Vec3f(x, y, z))

                    val u = toFloat(buffer, true, 24)
                    val v = toFloat(buffer, true, 28)

                    uvs.add(u to v)
                } else {
                    for (i in 0 until buffer.size / 3 / 2)
                        faces.add(toShort(buffer, true, true, i * 6 + 0) to toShort(buffer, true, true, i * 6 + 2) and toShort(buffer, true, true, i * 6 + 4))
                }
            }

            println("Something")
        }
    }
}