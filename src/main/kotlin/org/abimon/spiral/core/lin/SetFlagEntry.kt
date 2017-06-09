package org.abimon.spiral.core.lin

data class SetFlagEntry(val group: Int, val id: Int, val state: Int): LinScript {

    override fun getOpCode(): Int = 0x26
    override fun getRawArguments(): IntArray = intArrayOf(group, id, state)
}