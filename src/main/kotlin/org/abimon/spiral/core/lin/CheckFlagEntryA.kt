package org.abimon.spiral.core.lin

data class CheckFlagEntryA(val params: IntArray): LinScript {

    override fun getOpCode(): Int = 0x35
    override fun getRawArguments(): IntArray = params.copyOf()
}