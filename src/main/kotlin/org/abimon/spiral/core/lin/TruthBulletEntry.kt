package org.abimon.spiral.core.lin

data class TruthBulletEntry(val arg1: Int, val arg2: Int): LinScript {
    override fun getOpCode(): Int = 0x0C

    override fun getRawArguments(): IntArray = intArrayOf(arg1, arg2)
}