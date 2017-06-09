package org.abimon.spiral.core.lin

class EndFlagCheckEntry: LinScript {
    override fun getOpCode(): Int = 0x3C
    override fun getRawArguments(): IntArray = IntArray(0)

    override fun toString(): String = "EndFlagCheckEntry()"
}