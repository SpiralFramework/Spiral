package org.abimon.spiral.core.objects.scripting.wrd

data class VoiceLineEntry(val voiceLine: Int, val volumeControl: Int): WrdScript {
    override val opCode: Int = 0x19
    override val rawArguments: IntArray = intArrayOf(voiceLine shr 8, voiceLine % 256, volumeControl)
    override val cmdArguments: IntArray = intArrayOf(voiceLine, volumeControl)
}