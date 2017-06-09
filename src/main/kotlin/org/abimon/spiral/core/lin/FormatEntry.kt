package org.abimon.spiral.core.lin

data class FormatEntry(val format: Int) : LinScript {
    override fun getOpCode(): Int = 0x03
    override fun getRawArguments(): IntArray = intArrayOf(format)
}