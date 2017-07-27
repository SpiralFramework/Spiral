package org.abimon.spiral.core.lin.dr1

import org.abimon.spiral.core.lin.LinScript

data class LoadMapEntry(val room: Int, val state: Int, val padding: Int = 255): LinScript {

    override fun getOpCode(): Int = 0x15
    override fun getRawArguments(): IntArray = intArrayOf(room, state, padding)
}