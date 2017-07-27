package org.abimon.spiral.core.lin.dr2

import org.abimon.spiral.core.lin.LinScript

data class LoadMapDR2Entry(val room: Int, val state: Int, val padding: Int = 255, val unk: Int = 255): LinScript {

    override fun getOpCode(): Int = 0x15
    override fun getRawArguments(): IntArray = intArrayOf(room, state, padding, unk)
}