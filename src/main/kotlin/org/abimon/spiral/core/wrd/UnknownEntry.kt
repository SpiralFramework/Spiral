package org.abimon.spiral.core.wrd

data class UnknownEntry(val op: Int, val arguments: IntArray): WRDScript {
    override fun getOpCode(): Int = op

    override fun getRawArguments(): IntArray = arguments
}