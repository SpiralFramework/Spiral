package org.abimon.spiral.core.lin

data class CheckObjectEntry(val objID: Int): LinScript {

    override fun getOpCode(): Int = 0x29
    override fun getRawArguments(): IntArray = intArrayOf(objID)
}