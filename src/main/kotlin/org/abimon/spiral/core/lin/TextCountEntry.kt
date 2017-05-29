package org.abimon.spiral.core.lin

data class TextCountEntry(val lines: Int): LinScript {
    override fun getOpCode(): Int = 0x00
    override fun getRawArguments(): IntArray = intArrayOf(lines % 256, lines / 256)
}