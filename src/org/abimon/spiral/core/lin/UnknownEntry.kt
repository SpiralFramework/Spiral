package org.abimon.spiral.core.lin

data class UnknownEntry(val op: Int, val arguments: IntArray): LinScript {
    override fun getOpCode(): Int = op

    override fun getRawArguments(): IntArray = arguments

}