package org.abimon.spiral.core.objects.scripting.wrd

data class SpeakerEntry(val charID: Int): WrdScript {
    override val opCode: Int = 0x53
    override val rawArguments: IntArray = intArrayOf(charID)
}