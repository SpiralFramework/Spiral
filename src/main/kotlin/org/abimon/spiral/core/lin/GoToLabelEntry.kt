package org.abimon.spiral.core.lin

data class GoToLabelEntry(val id: Int): LinScript {
    constructor(rem: Int, major: Int): this(rem + major * 256)

    override fun getOpCode(): Int = 0x34
    override fun getRawArguments(): IntArray = intArrayOf(id % 256, id / 256)
}