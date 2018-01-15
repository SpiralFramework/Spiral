package org.abimon.spiral.core.lin.dr1

import org.abimon.spiral.core.lin.LinScript

class WaitForInputEntry : LinScript {

    override fun getOpCode(): Int = 0x3A
    override fun getRawArguments(): IntArray = IntArray(0)

    override fun toString(): String = "WaitForInputEntry()"
}