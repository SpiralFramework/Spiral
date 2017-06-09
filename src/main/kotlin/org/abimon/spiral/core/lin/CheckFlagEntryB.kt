package org.abimon.spiral.core.lin

data class CheckFlagEntryB(val params: IntArray): LinScript {

    override fun getOpCode(): Int = 0x36
    override fun getRawArguments(): IntArray = params.copyOf()
}