package org.abimon.spiral.core.objects.scripting.wrd

data class ScriptEntry(val scriptID: Int, val labelID: Int): WrdScript {
    override val opCode: Int = 0x10
    override val rawArguments: IntArray = intArrayOf(scriptID, labelID)
}