package org.abimon.spiral.core.objects.models

import org.abimon.spiral.core.utils.UV
import org.abimon.spiral.core.utils.Vertex

open class GMOModelChunk(open val chunkType: Int, open val chunkHeaderSize: Int, open val chunkSize: Long, open val header: IntArray) {
    operator fun component1(): Int = chunkType
    operator fun component2(): Int = chunkHeaderSize
    operator fun component3(): Long = chunkSize
    operator fun component4(): IntArray = header

    override fun toString(): String = "GMOModelChunk(chunkType=$chunkType, chunkHeaderSize=$chunkHeaderSize, chunkSize=$chunkSize, header=${header.contentToString()})"
}

class GMOSubfileChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val subchunks: List<GMOModelChunk>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header) {
    operator fun component5(): List<GMOModelChunk> = subchunks

    override fun toString(): String = "GMOSubfileChunk(chunkType=$chunkType, chunkHeaderSize=$chunkHeaderSize, chunkSize=$chunkSize, header=${header.contentToString()}), subchunks=$subchunks"
}

class GMOModelSurfaceChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val subchunks: List<GMOModelChunk>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header) {
    operator fun component5(): List<GMOModelChunk> = subchunks

    override fun toString(): String = "GMOModelSurfaceChunk(chunkType=$chunkType, chunkHeaderSize=$chunkHeaderSize, chunkSize=$chunkSize, header=${header.contentToString()}), subchunks=$subchunks"
}

class GMOMeshChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val subchunks: List<GMOModelChunk>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header) {
    operator fun component5(): List<GMOModelChunk> = subchunks

    override fun toString(): String = "GMOMeshChunk(chunkType=$chunkType, chunkHeaderSize=$chunkHeaderSize, chunkSize=$chunkSize, header=${header.contentToString()}), subchunks=$subchunks"
}

class GMOMaterialChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val subchunks: List<GMOModelChunk>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header) {
    operator fun component5(): List<GMOModelChunk> = subchunks

    override fun toString(): String = "GMOMaterialChunk(chunkType=$chunkType, chunkHeaderSize=$chunkHeaderSize, chunkSize=$chunkSize, header=${header.contentToString()}, subchunks=$subchunks)"
}

class GMOVertexArrayChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val uvs: List<UV>, val vertices: List<Vertex>, val padding: List<ByteArray>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header)

class GMOMeshFacesChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val arIndex: Int, val unknown: Int, val primType: Int, val faces: List<IntArray>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header)

class GMOMeshMaterialInfoChunk(val material: Int): GMOModelChunk(0x8061, 0, 10, intArrayOf())

class GMOMaterialNameChunk(val materialName: String, dataSize: Long, header: IntArray): GMOModelChunk(0x000A, header.size, dataSize, header)

class GMOTextureReferenceChunk(val padding: Long, val textureReference: Int, val unk: Int, header: IntArray): GMOModelChunk(0x0009, header.size, 36, header)

class GMO8014Chunk(val floats: FloatArray, header: IntArray): GMOModelChunk(0x8014, header.size, 20, header)