package org.abimon.spiral.core.lin

data class MusicEntry(val musicID: Int, val transition: Int, val volume: Int = 100): LinScript {
    override fun getOpCode(): Int = 0x09
    override fun getRawArguments(): IntArray = intArrayOf(musicID, transition, volume)
}