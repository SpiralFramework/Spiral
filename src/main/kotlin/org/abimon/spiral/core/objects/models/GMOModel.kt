package org.abimon.spiral.core.objects.models

import org.abimon.spiral.core.utils.*
import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.safeCast

class GMOModel(val dataSource: () -> InputStream) {
    companion object {
        val NORMAL_MASK = makeMask(5, 6)
        val NORMAL_SHIFT = 5

        val POSITION_MASK = makeMask(7, 8)
        val POSITION_SHIFT = 7

        val WEIGHT_MASK = makeMask(9, 10)
        val WEIGHT_SHIFT = 9

        val INDEX_MASK = makeMask(11, 12)
        val INDEX_SHIFT = 11

        val NUMBER_VERTICES_MASK = makeMask(18, 19, 20)
        val NUMBER_VERTICES_SHIFT = 18
    }


    val chunks: Array<GMOModelChunk>

    init {
        chunks = dataSource().use { rawStream ->
            val stream = CountingInputStream(rawStream)
            val magic = stream.readString(12)
            if(magic != "OMG.00.1PSP\u0000")
                throw IllegalArgumentException("$dataSource is not a valid GMO model ($magic ≠ \"OMG.00.1PSP\u0000\")")
            val padding = stream.readInt32LE()

            val chunks = stream.readChunks(dataSource, null).toTypedArray()
            return@use chunks
        }
    }

    fun CountingInputStream.readChunks(dataSource: () -> InputStream, parentChunk: GMOModelChunk?): List<GMOModelChunk> {
        val list: MutableList<GMOModelChunk> = ArrayList()

        try {
            while (available() > 0) {
                val chunkID = this.readInt16LE()
                val headerSize = this.readInt16LE()
                val dataSize = this.readUInt32LE()
                val header = if (headerSize <= 0) IntArray(0) else ByteArray(headerSize - 8).apply { this@readChunks.read(this) }.map { byte -> byte.toInt() and 0xFF }.toIntArray()
                val chunk = GMOModelChunk(chunkID, headerSize, dataSize, header)
                val substream = WindowedInputStream(dataSource(), streamOffset, dataSize - headerSize.coerceAtLeast(8))

                try {
                    when (chunkID) {
                        0x02 -> list.addAll(substream.readChunks(dataSource, chunk))
                        0x03 -> list.add(GMOSubfileChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))
                        0x05 -> list.add(GMOModelSurfaceChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))
                        0x06 -> list.add(GMOMeshChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))
                        0x07 -> {
                            val unk1 = substream.readInt32LE()

                            val normalFormat = (unk1 and NORMAL_MASK) shr NORMAL_SHIFT
                            val indexFormat = (unk1 and INDEX_MASK) shr INDEX_SHIFT
                            val weightFormat = (unk1 and WEIGHT_MASK) shr WEIGHT_SHIFT
                            val positionFormat = (unk1 and POSITION_MASK) shr POSITION_SHIFT
                            val numberOfVertices = (unk1 and NUMBER_VERTICES_MASK) shr NUMBER_VERTICES_SHIFT

                            println("$normalFormat|$indexFormat|$weightFormat|$positionFormat|$numberOfVertices")

                            val numVerts = substream.readInt32LE() //toInt(header, true, true, 4)
                            val unk2 = substream.readInt64LE()

                            val vertSize = (dataSize - headerSize - 16) / numVerts

//                            val uvs: MutableList<UV> = ArrayList()
//                            val vertices: MutableList<Vertex> = ArrayList()

                            val verts: MutableList<Triple<UV, Vertex, ByteArray>> = ArrayList()

                            for (i in 0 until numVerts) {

//                            val u = substream.readInt16LE() / 65536.0f
//                            val v = substream.readInt16LE() / 65536.0f
//
//                            val index = substream.readInt16LE()
//
//                            val x = substream.readInt16LE()
//                            val y = substream.readInt16LE()
//                            val z = substream.readInt16LE()

                                val u = substream.readFloatLE()
                                val v = substream.readFloatLE()

                                val padding = substream.readXBytes((vertSize - 20).toInt()) //Not sure what this is

                                val x = substream.readFloatLE()
                                val y = substream.readFloatLE()
                                val z = substream.readFloatLE()

//                                uvs.add(u to v)
//                                vertices.add(x to y and z)

                                verts.add((u to v) to (x to y and z) and (padding))
                            }

                            //val sortedVerts = verts.sortedBy { (_, _, id) -> id }.asReversed()
                            val uvs = verts.map { (uv) -> uv }
                            val vertices = verts.map { (_, vertice) -> vertice }
                            val padding = verts.map { (_, _, pad) -> pad }

                            //println(header.joinToString(" ", prefix = "$numVerts: ") { byte -> (byte.toInt() and 0xFF).toString(16).padStart(2, '0') })
                            list.add(GMOVertexArrayChunk(chunkID, headerSize, dataSize, header, uvs, vertices, padding))
                        }
                        0x08 -> list.add(GMOMaterialChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))
                        0x09 -> {
                            val padding = substream.readInt64BE()
                            val materialIndex = substream.readInt16LE() - 8192
                            val unk = substream.readInt16LE()
                            list.add(GMOTextureReferenceChunk(padding, materialIndex, unk, header))
                        }
                        0x0A -> {
                            val padding = substream.readInt64BE()
                            val name = substream.readNullTerminatedString()
                            list.add(GMOMaterialNameChunk(name, dataSize, header))
                        }

                        0x8014 -> {
                            val floats = FloatArray(6) { substream.readFloatLE() }
                            list.add(GMO8014Chunk(floats, header))
                        }

                        0x8061 -> {
                            val materialID = substream.readInt16LE() - 8192
                            list.add(GMOMeshMaterialInfoChunk(materialID))
                        }
                        0x8066 -> {
                            val arIndex = substream.readInt16LE() - 4096
                            val unk = substream.readInt16LE()
                            val primType = substream.readInt32LE()
                            val faces: MutableList<IntArray> = ArrayList()

                            when (primType) {
                                3 -> {
                                    val numIdx = substream.readInt32LE()
                                    val idxStride = substream.readInt32LE()

                                    for (i in 0L until (numIdx * idxStride) / 3)
                                        faces.add(intArrayOf(substream.readInt16LE(), substream.readInt16LE(), substream.readInt16LE()))
                                }
                                4 -> {
                                    val idxStride = substream.readInt32LE()
                                    val numIdx = substream.readInt32LE()

                                    for (i in 0L until (numIdx * idxStride) / 4) {
//                                    val a = substream.readInt16LE()
//                                    val b = substream.readInt16LE()
//                                    val c = substream.readInt16LE()
//                                    val d = substream.readInt16LE()
//
//                                    faces.add(a to b and c)
//                                    faces.add(a to c and d)

                                        faces.add(intArrayOf(
                                                substream.readInt16LE(), substream.readInt16LE(),
                                                substream.readInt16LE(), substream.readInt16LE()
                                        ))
                                    }
                                }
                                else -> println("Missing faces primType $primType")
                            }

                            list.add(GMOMeshFacesChunk(chunkID, headerSize, dataSize, header, arIndex, unk, primType, faces))
                        }

                        else -> {
                            list.add(chunk)
                            println("Missing chunk ID 0x${chunkID.toString(16)} with parent $parentChunk")
                        }
                    }

                    if (dataSize > 0)
                        skip(dataSize - headerSize.coerceAtLeast(8))
                } finally {
                    substream.close()
                }
            }
        } catch (indexOutOfBounds: IndexOutOfBoundsException) {
            indexOutOfBounds.printStackTrace()
        }

        return list
    }

    fun List<GMOModelChunk>.findChunk(predicate: (GMOModelChunk) -> Boolean): GMOModelChunk? {
        forEach { chunk ->
            if (predicate(chunk))
                return chunk

            when (chunk) {
                is GMOSubfileChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
                is GMOModelSurfaceChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
                is GMOMeshChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
                is GMOMaterialChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            }
        }

        return null
    }

    fun List<GMOModelChunk>.filterChunks(predicate: (GMOModelChunk) -> Boolean): List<GMOModelChunk> {
        return flatMap { chunk ->
            if (predicate(chunk))
                return@flatMap listOf(chunk)

            when (chunk) {
                is GMOSubfileChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                is GMOModelSurfaceChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                is GMOMeshChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                is GMOMaterialChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                else -> return@flatMap emptyList<GMOModelChunk>()
            }
        }
    }

    fun Array<GMOModelChunk>.findChunk(predicate: (GMOModelChunk) -> Boolean): GMOModelChunk? {
        forEach { chunk ->
            if (predicate(chunk))
                return chunk

            when (chunk) {
                is GMOSubfileChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
                is GMOModelSurfaceChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
                is GMOMeshChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
                is GMOMaterialChunk -> return chunk.subchunks.findChunk(predicate) ?: return@forEach
            }
        }

        return null
    }

    fun Array<GMOModelChunk>.filterChunks(predicate: (GMOModelChunk) -> Boolean): List<GMOModelChunk> {
        return flatMap { chunk ->
            if (predicate(chunk))
                return@flatMap listOf(chunk)

            when (chunk) {
                is GMOSubfileChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                is GMOModelSurfaceChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                is GMOMeshChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                is GMOMaterialChunk -> return@flatMap chunk.subchunks.filterChunks(predicate)
                else -> return@flatMap emptyList<GMOModelChunk>()
            }
        }
    }

    fun findChunk(predicate: (GMOModelChunk) -> Boolean): GMOModelChunk? = chunks.findChunk(predicate)
    fun filterChunks(predicate: (GMOModelChunk) -> Boolean): List<GMOModelChunk> = chunks.filterChunks(predicate)

    fun <T: GMOModelChunk> findChunk(klass: KClass<T>): T? = klass.safeCast(chunks.findChunk { klass.isInstance(it) })
    fun <T: GMOModelChunk> filterChunks(klass: KClass<T>): List<T> = chunks.filterChunks { chunk -> klass.isInstance(chunk) }.map { chunk -> klass.cast(chunk) }
}