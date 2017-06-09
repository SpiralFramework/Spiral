package org.abimon.spiral.core.lin

data class TextCountEntry(val lines: Int): LinScript {
    constructor(rem: Int, main: Int): this(rem + main * 256)

    override fun getOpCode(): Int = 0x00
    override fun getRawArguments(): IntArray = intArrayOf(lines % 256, lines / 256)
}