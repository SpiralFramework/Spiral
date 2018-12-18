package org.abimon.spiral.core.objects.scripting.wrd

data class VoiceLineEntry(val voiceLine: Int, val volumeControl: Int): WrdScript {
    override val opCode: Int = 0x19
    override val rawArguments: IntArray = intArrayOf(voiceLine, volumeControl)
}