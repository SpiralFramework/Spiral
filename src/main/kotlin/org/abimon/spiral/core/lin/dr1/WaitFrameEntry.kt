package org.abimon.spiral.core.lin.dr1

import org.abimon.spiral.core.lin.LinScript

class WaitFrameEntry: LinScript {
    
    override fun getOpCode(): Int = 0x3B
    override fun getRawArguments(): IntArray = IntArray(0)

    override fun toString(): String = "WaitFrameEntry()"
}