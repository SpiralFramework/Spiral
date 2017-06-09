package org.abimon.spiral.core.lin

data class ChangeUIEntry(val element: Int, val state: Int): LinScript {

    override fun getOpCode(): Int = 0x25
    override fun getRawArguments(): IntArray = intArrayOf(element, state)
}