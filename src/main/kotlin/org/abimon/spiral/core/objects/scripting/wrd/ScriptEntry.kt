package org.abimon.spiral.core.objects.scripting.wrd

data class ScriptEntry(val scriptID: Int, val labelID: Int): WrdScript {
    override val opCode: Int = 0x10
    override val rawArguments: IntArray = intArrayOf(scriptID shr 8, scriptID % 256, labelID shr 8, labelID % 256)
    override val cmdArguments: IntArray = intArrayOf(scriptID, labelID)
}