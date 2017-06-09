package org.abimon.spiral.core.lin

class WaitForInputEntry : LinScript {

    override fun getOpCode(): Int = 0x3A
    override fun getRawArguments(): IntArray = IntArray(0)
}