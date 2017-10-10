package org.abimon.spiral.core.lin

data class SpeakerEntry(val charID: Int): LinScript {
    override fun getOpCode(): Int = 0x21
    override fun getRawArguments(): IntArray = intArrayOf(charID)
}