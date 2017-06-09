package org.abimon.spiral.core.lin

class StopScriptEntry: LinScript {

    override fun getOpCode(): Int = 0x1A
    override fun getRawArguments(): IntArray = IntArray(0)
}