package org.abimon.spiral.core.lin

class WaitFrameEntry: LinScript {
    
    override fun getOpCode(): Int = 0x3B
    override fun getRawArguments(): IntArray = IntArray(0)
}