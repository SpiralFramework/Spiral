package org.abimon.spiral.core.objects.scripting.wrd

data class TextEntry(val id: Int): WrdScript {
    override val opCode: Int = 0x46
    override val rawArguments: IntArray = intArrayOf(id shr 8, id % 256)
    override val cmdArguments: IntArray = intArrayOf(id)
}