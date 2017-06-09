package org.abimon.spiral.core.lin

data class MovieEntry(val id: Int): LinScript {
    constructor(main: Int, rem: Int): this(main * 256 + rem)
    override fun getOpCode(): Int = 0x05
    override fun getRawArguments(): IntArray = intArrayOf(id / 256, id % 256)
}