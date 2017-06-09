package org.abimon.spiral.core.lin

data class SetStudentTitleEntry(val charID: Int, val arg2: Int, val state: Int): LinScript {
    constructor(charID: Int, state: Int): this(charID, 0, state)

    override fun getOpCode(): Int = 0x0F
    override fun getRawArguments(): IntArray = intArrayOf(charID, arg2, state)
}