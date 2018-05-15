package org.abimon.spiral.core.objects.models

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.objects.archives.srd.*
import org.abimon.spiral.core.utils.*
import java.io.InputStream

class SRDIModel(val meshInfo: SRD, val dataSource: () -> InputStream) {
    companion object {
        var MAP_INITIAL_SKIP = 0L
    }

    val meshes: Array<SRDIMesh>

    init {
        val vertexMeshEntries = meshInfo.entries.filterIsInstance(VTXEntry::class.java)
        val meshEntries = meshInfo.entries.filterIsInstance(MSHEntry::class.java)
        val materialEntries = meshInfo.entries.filterIsInstance(MATEntry::class.java)
        val textureInfoEntries = meshInfo.entries.filterIsInstance(TXIEntry::class.java)
        val textureEntries = meshInfo.entries.filterIsInstance(TXREntry::class.java)

        meshes = vertexMeshEntries.map { vtx ->
            val vertices: MutableList<Vertex> = ArrayList()
            val faces: MutableList<TriFace> = ArrayList()

            dataSource().use { stream ->
                stream.skip(vtx.faceBlock.start.toLong())

                for (i in 0 until vtx.faceBlock.length / 6) {
                    val a = stream.readInt16LE()
                    val b = stream.readInt16LE()
                    val c = stream.readInt16LE()

                    faces.add(TriFace(a, b, c))
                }
            }

            //assertOrThrow(vtx.meshType != EnumSRDIMeshType.UNKNOWN, IllegalStateException())
            //println("${vtx.rsiEntry.name}=${vtx.meshType}")

            val mesh = when (vtx.meshType) {
                EnumSRDIMeshType.MAP -> {
                    dataSource().use { stream ->
                        stream.skip(vtx.vertexBlock.start.toLong())

                        for (i in 0 until vtx.vertexCount) {
                            val x = stream.readFloatLE().roundToPrecision()
                            val y = stream.readFloatLE().roundToPrecision()
                            val z = stream.readFloatLE().roundToPrecision()

                            //stream.skip(20)
                            stream.skip(56 - 12)

                            vertices.add(Vertex(x, y, z))
                        }
                    }

                    SRDIMapMesh(vertices.toTypedArray(), emptyArray(), faces.toTypedArray(), emptyArray())
                }
                EnumSRDIMeshType.BONES -> {
                    dataSource().use { stream ->
                        stream.skip(vtx.vertexBlock.start.toLong())

                        for (i in 0 until vtx.vertexCount) {
                            val x = stream.readFloatLE().roundToPrecision()
                            val y = stream.readFloatLE().roundToPrecision()
                            val z = stream.readFloatLE().roundToPrecision()

                            stream.skip(36)

                            vertices.add(Vertex(x, y, z))
                        }
                    }

                    val normals: MutableList<Vertex> = ArrayList()
                    val uvs: MutableList<UV> = ArrayList()

                    dataSource().use { stream ->
                        stream.skip(vtx.vertexBlock.start.toLong())

                        for (i in 0 until vtx.vertexCount) {
                            stream.skip(16)

                            val nx = stream.readFloatLE().roundToPrecision()
                            val ny = stream.readFloatLE().roundToPrecision()
                            val nz = stream.readFloatLE().roundToPrecision()

                            stream.skip(20)

                            normals.add(Vertex(nx, ny, nz))
                        }

                        stream.skip(32L * vtx.vertexCount)

                        for (i in 0 until vtx.vertexCount) {
                            val u = stream.readFloatLE().roundToPrecision()
                            val v = stream.readFloatLE().roundToPrecision()

                            uvs.add(UV(u, v))
                        }
                    }

                    SRDIBonesMesh(vertices.toTypedArray(), uvs.toTypedArray(), faces.toTypedArray(), emptyArray(), emptyArray()).apply { this.normals = normals.toTypedArray() }
                }
                EnumSRDIMeshType.MAP_UNK -> {
                    dataSource().use { stream ->
                        stream.skip(vtx.vertexBlock.start.toLong())

                        for (i in 0 until vtx.vertexCount) {
                            val x = stream.readFloatLE().roundToPrecision()
                            val y = stream.readFloatLE().roundToPrecision()
                            val z = stream.readFloatLE().roundToPrecision()

                            stream.skip(20)

                            vertices.add(Vertex(x, y, z))
                        }
                    }

                    val uvs: MutableList<UV> = ArrayList()

//                    dataSource().use { stream ->
//                        stream.skip(vtx.vertexBlock.start.toLong())
//
//                        for (i in 0 until vtx.vertexCount) {
//                            stream.skip(24)
//
//                            val u = stream.readFloatLE().roundToPrecision()
//                            val v = stream.readFloatLE().roundToPrecision()
//
//                            stream.skip(16)
//
//                            uvs.add(UV(u, v))
//                        }
//                    }

                    SRDIMesh(vertices.toTypedArray(), uvs.toTypedArray(), faces.toTypedArray())
                }
                EnumSRDIMeshType.MAP_UNK_2 -> {
                    dataSource().use { stream ->
                        stream.skip(vtx.vertexBlock.start.toLong())

                        for (i in 0 until vtx.vertexCount) {
                            val x = stream.readFloatLE().roundToPrecision()
                            val y = stream.readFloatLE().roundToPrecision()
                            val z = stream.readFloatLE().roundToPrecision()

                            //stream.skip(20)
                            stream.skip(56 - 12)

                            vertices.add(Vertex(x, y, z))
                        }
                    }

                    SRDIMesh(vertices.toTypedArray(), emptyArray(), faces.toTypedArray())
                }
                else -> {
                    val normals: MutableList<Vertex> = ArrayList()
                    dataSource().use { stream ->
                        stream.skip(vtx.vertexBlock.start.toLong())

                        for (i in 0 until vtx.vertexCount) {
                            val x = stream.readFloatLE().roundToPrecision()
                            val y = stream.readFloatLE().roundToPrecision()
                            val z = stream.readFloatLE().roundToPrecision()

                            stream.skip(4)

                            val nx = stream.readFloatLE().roundToPrecision()
                            val ny = stream.readFloatLE().roundToPrecision()
                            val nz = stream.readFloatLE().roundToPrecision()

                            stream.skip(20)

                            vertices.add(Vertex(x, y, z))
                            normals.add(Vertex(nx, ny, nz))
                        }
                    }

                    val uvs: MutableList<UV> = ArrayList()

                    dataSource().use { stream ->
                        stream.skip(vtx.vertexBlock.start.toLong())

                        for (i in 0 until vtx.vertexCount) {
                            stream.skip(24)

                            val u = stream.readFloatLE().roundToPrecision()
                            val v = stream.readFloatLE().roundToPrecision()

                            stream.skip(16)

                            uvs.add(UV(u, v))
                        }
                    }

                    SRDIMesh(vertices.toTypedArray(), uvs.toTypedArray(), faces.toTypedArray()).apply { this.normals = normals.toTypedArray() }
                }
            }

            mesh.name = vtx.rsiEntry.name

            val meshEntry = meshEntries.first { entry -> entry.meshName == vtx.rsiEntry.name }
            val materialEntry = materialEntries.first { entry -> entry.rsiEntry.name == meshEntry.materialName }

            mesh.materialName = meshEntry.materialName
            mesh.textures = materialEntry.materials.mapValues { (_, textureName) ->
                textureInfoEntries.first { entry -> entry.rsiEntry.name == textureName }.filename
            }.mapValues { (_, textureName) -> textureEntries.first { entry -> entry.rsiEntry.name == textureName } }

            return@map mesh
        }.toTypedArray()
    }
}