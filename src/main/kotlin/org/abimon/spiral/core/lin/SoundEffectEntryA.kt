package org.abimon.spiral.core.lin

data class SoundEffectEntryA(val sfxID: Int, val transition: Int, val arg3: Int = 0): LinScript {
    override fun getOpCode(): Int = 0x0A
    override fun getRawArguments(): IntArray = intArrayOf(sfxID, transition, arg3)
}