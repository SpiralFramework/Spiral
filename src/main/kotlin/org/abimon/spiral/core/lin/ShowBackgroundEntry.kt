package org.abimon.spiral.core.lin

data class ShowBackgroundEntry(val bgID: Int, val state: Int): LinScript {
    constructor(rem: Int, major: Int, state: Int): this(rem + major * 256, state)

    override fun getOpCode(): Int = 0x30
    override fun getRawArguments(): IntArray = intArrayOf(bgID % 256, bgID / 256, state)
}