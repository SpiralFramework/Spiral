package org.abimon.spiral.core.wrd

interface WRDScript {
    fun getOpCode(): Int
    fun getRawArguments(): IntArray
}