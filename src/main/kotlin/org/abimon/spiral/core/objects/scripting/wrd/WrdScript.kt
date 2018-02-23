package org.abimon.spiral.core.objects.scripting.wrd

interface WrdScript {
    val opCode: Int
    val rawArguments: IntArray
    val cmdArguments: IntArray
        get() = IntArray(rawArguments.size / 2) { index -> return@IntArray ((rawArguments[index * 2] shl 8) or rawArguments[index * 2 + 1]) }
}