package org.abimon.spiral.core.lin

interface LinScript {
    fun getOpCode(): Int
    fun getRawArguments(): IntArray
}