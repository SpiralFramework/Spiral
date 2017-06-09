package org.abimon.spiral.core.lin

data class SoundEffectEntryB(val arg1: Int, val arg2: Int = 100): LinScript {
    override fun getOpCode(): Int = 0x0B
    override fun getRawArguments(): IntArray = intArrayOf(arg1, arg2)
}