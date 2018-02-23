package org.abimon.spiral.core.objects.scripting.wrd

data class LabelEntry(val labelID: Int): WrdScript {
    override val opCode: Int = 0x14
    override val rawArguments: IntArray = intArrayOf(labelID shr 8, labelID % 256)
    override val cmdArguments: IntArray = intArrayOf(labelID)
}