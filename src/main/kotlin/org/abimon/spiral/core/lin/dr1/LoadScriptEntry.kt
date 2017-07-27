package org.abimon.spiral.core.lin.dr1

import org.abimon.spiral.core.lin.LinScript

data class LoadScriptEntry(val chapter: Int, val scene: Int, val room: Int): LinScript {

    override fun getOpCode(): Int = 0x19
    override fun getRawArguments(): IntArray = intArrayOf(chapter, scene, room)
}