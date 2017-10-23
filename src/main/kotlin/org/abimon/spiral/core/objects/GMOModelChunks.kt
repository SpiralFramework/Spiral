package org.abimon.spiral.core.objects

import org.abimon.spiral.util.TriFace
import org.abimon.spiral.util.UV
import org.abimon.spiral.util.Vertex

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

class GMOVertexArrayChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val uvs: List<UV>, val vertices: List<Vertex>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header)

class GMOMeshFacesChunk(chunkType: Int, chunkHeaderSize: Int, chunkSize: Long, header: IntArray, val arIndex: Int, val unknown: Int, val primType: Long, val faces: List<TriFace>):
        GMOModelChunk(chunkType, chunkHeaderSize, chunkSize, header)