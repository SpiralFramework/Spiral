package org.abimon.spiral.core.objects.models

import org.abimon.spiral.core.*
import org.abimon.spiral.util.*
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import org.abimon.visi.lang.and
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

class GMOModel(val dataSource: DataSource) {
    val chunks: Array<GMOModelChunk>

    init {
        chunks = dataSource.use { rawStream ->
            val stream = CountingInputStream(rawStream)
            val magic = stream.readString(12)
            if(magic != "OMG.00.1PSP\u0000")
                throw IllegalArgumentException("$dataSource is not a valid GMO model ($magic ≠ \"OMG.00.1PSP\u0000\")")
            val padding = stream.readInt()

            return@use stream.readChunks(dataSource, null).toTypedArray()
        }
    }

    fun CountingInputStream.readChunks(dataSource: DataSource, parentChunk: GMOModelChunk?): List<GMOModelChunk> {
        val list: MutableList<GMOModelChunk> = ArrayList()

        try {
            while (available() > 0) {
                val chunkID = this.readUnsureShort(true, true) ?: break
                val headerSize = this.readUnsureShort(true, true) ?: break
                val dataSize = this.readUnsureInt(true, true) ?: break
                val header = if (headerSize <= 0) IntArray(0) else this.read(headerSize - 8).toIntArray()
                val chunk = GMOModelChunk(chunkID, headerSize, dataSize, header)
                val substream = OffsetInputStream(dataSource.seekableInputStream, streamOffset, dataSize - headerSize.coerceAtLeast(8))

                when (chunkID) {
                    0x02 -> list.addAll(substream.readChunks(dataSource, chunk))
                    0x03 -> list.add(GMOSubfileChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))
                    0x05 -> list.add(GMOModelSurfaceChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))
                    0x06 -> list.add(GMOMeshChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))
                    0x07 -> {
                        val unk1 = substream.readInt()
                        val numVerts = substream.readInt(true, true) //toInt(header, true, true, 4)
                        val unk2 = substream.readLong()

                        val vertSize = (dataSize - headerSize - 16) / numVerts

                        val uvs: MutableList<UV> = ArrayList()
                        val vertices: MutableList<Vertex> = ArrayList()

                        for (i in 0 until numVerts) {

//                            val u = substream.readShort(true, true) / 65536.0f
//                            val v = substream.readShort(true, true) / 65536.0f
//
//                            val index = substream.readShort(true, true)
//
//                            val x = substream.readShort(true, true)
//                            val y = substream.readShort(true, true)
//                            val z = substream.readShort(true, true)

                            val u = substream.readFloat(true, true)
                            val v = substream.readFloat(true, true)

                            substream.skip(12) //Not sure what this is

                            val x = substream.readFloat(true, true)
                            val y = substream.readFloat(true, true)
                            val z = substream.readFloat(true, true)

                            uvs.add(u to v)
                            vertices.add(x to y and z)
                        }

                        list.add(GMOVertexArrayChunk(chunkID, headerSize, dataSize, header, uvs, vertices))
                    }
                    0x08 -> list.add(GMOMaterialChunk(chunkID, headerSize, dataSize, header, substream.readChunks(dataSource, chunk)))

                    0x0A -> {
                        val padding = substream.read(8)
                        val name = substream.readZeroString()
                    }

                    0x8066 -> {
                        val arIndex = substream.readShort(true, true)
                        val unk = substream.readShort(true, true)
                        val primType = substream.readInt(true, true)
                        val faces: MutableList<IntArray> = ArrayList()

                        when(primType) {
                            3L -> {
                                val numIdx = substream.readInt(true, true)
                                val idxStride = substream.readInt(true, true)

                                for(i in 0L until (numIdx * idxStride) / 3)
                                    faces.add(intArrayOf(substream.readShort(true, true), substream.readShort(true, true), substream.readShort(true, true)))
                            }
                            4L -> {
                                val idxStride = substream.readInt(true, true)
                                val numIdx = substream.readInt(true, true)

                                for (i in 0L until (numIdx * idxStride) / 4) {
//                                    val a = substream.readShort(true, true)
//                                    val b = substream.readShort(true, true)
//                                    val c = substream.readShort(true, true)
//                                    val d = substream.readShort(true, true)
//
//                                    faces.add(a to b and c)
//                                    faces.add(a to c and d)

                                    faces.add(intArrayOf(
                                            substream.readShort(true, true), substream.readShort(true, true),
                                            substream.readShort(true, true), substream.readShort(true, true)
                                    ))
                                }
                            }
                            else -> debug("Missing faces primType $primType")
                        }

                        list.add(GMOMeshFacesChunk(chunkID, headerSize, dataSize, header, arIndex, unk, primType, faces))
                    }

                    else -> {
                        list.add(chunk)
                        debug("Missing chunk ID 0x${chunkID.toString(16)} with parent $parentChunk")
                    }
                }

                if (dataSize > 0)
                    skip(dataSize - headerSize.coerceAtLeast(8))
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

    fun findChunk(predicate: (GMOModelChunk) -> Boolean): GMOModelChunk? = chunks.findChunk(predicate)

    fun <T: GMOModelChunk> findChunk(klass: KClass<T>): T? = klass.safeCast(chunks.findChunk { klass.isInstance(it) })
}