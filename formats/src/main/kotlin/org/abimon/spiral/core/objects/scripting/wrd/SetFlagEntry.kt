package org.abimon.spiral.core.objects.scripting.wrd

data class SetFlagEntry(val valueID: Int, val flagID: Int): WrdScript {
    override val opCode: Int = 0x00
    override val rawArguments: IntArray = intArrayOf(valueID, flagID)
}