package org.abimon.spiral.core.lin

data class FilterEntry(val arg1: Int, val filter: Int, val arg3: Int, val arg4: Int): LinScript {
    constructor(filter: Int): this(1, filter, 0, 0)

    override fun getOpCode(): Int = 0x04
    override fun getRawArguments(): IntArray = intArrayOf(arg1, filter, arg3, arg4)
}