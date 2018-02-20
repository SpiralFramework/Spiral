package org.abimon.spiral.core.objects.scripting.lin

data class VoiceLineEntry(val characterID: Int, val chapter: Int, val voiceLineID: Int, val volume: Int = 100): LinScript {
    constructor(op: Int, args: IntArray): this(args[0], args[1], (args[2] shl 8) or args[3], args[4])

    override val opCode: Int = 0x08
    override val rawArguments: IntArray = intArrayOf(characterID, chapter, voiceLineID shr 8, voiceLineID % 256, volume)
}