package org.abimon.spiral.core.lin

data class ChoiceEntry(val arg1: Int): LinScript {

    override fun getOpCode(): Int = 0x2B
    override fun getRawArguments(): IntArray = intArrayOf(arg1)
}