package org.abimon.spiral.core.lin

data class CheckCharacterEntry(val charID: Int): LinScript {

    override fun getOpCode(): Int = 0x27
    override fun getRawArguments(): IntArray = intArrayOf(charID)
}