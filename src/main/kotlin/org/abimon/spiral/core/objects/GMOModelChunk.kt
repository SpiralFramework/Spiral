package org.abimon.spiral.core.objects

data class GMOModelChunk(val chunkType: Int, val chunkHeaderSize: Int, val chunkSize: Long, val header: IntArray)