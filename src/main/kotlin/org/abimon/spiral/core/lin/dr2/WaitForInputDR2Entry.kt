package org.abimon.spiral.core.lin.dr2

import org.abimon.spiral.core.lin.LinScript

class WaitForInputDR2Entry : LinScript {
    override fun getOpCode(): Int = 0x4B
    override fun getRawArguments(): IntArray = IntArray(0)

    override fun toString(): String = "WaitForInputEntry()"
}