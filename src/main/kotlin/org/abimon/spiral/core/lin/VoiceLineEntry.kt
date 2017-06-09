package org.abimon.spiral.core.lin

data class VoiceLineEntry(val characterID: Int, val chapter: Int, val voiceLineID: Int, val volume: Int = 100): LinScript {
    constructor(characterID: Int, chapter: Int, major: Int, minor: Int, volume: Int): this(characterID, chapter, major * 256 + minor, volume)

    override fun getOpCode(): Int = 0x08

    override fun getRawArguments(): IntArray = intArrayOf(characterID, chapter, voiceLineID / 256, voiceLineID % 256, volume)
}