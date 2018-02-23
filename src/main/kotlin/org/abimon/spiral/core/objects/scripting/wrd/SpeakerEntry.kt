package org.abimon.spiral.core.objects.scripting.wrd

data class SpeakerEntry(val charID: Int): WrdScript {
    override val opCode: Int = 0x53
    override val rawArguments: IntArray = intArrayOf(charID shr 8, charID % 256)
    override val cmdArguments: IntArray = intArrayOf(charID)
}