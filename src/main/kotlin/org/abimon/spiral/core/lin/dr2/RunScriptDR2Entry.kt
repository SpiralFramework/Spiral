package org.abimon.spiral.core.lin.dr2

import org.abimon.spiral.core.lin.LinScript

data class RunScriptDR2Entry(val chapter: Int, val scene: Int, val room: Int): LinScript {
    constructor(chapter: Int, scene: Int, sceneRem: Int, room: Int, roomRem: Int): this(chapter, scene * 256 + sceneRem, room * 256 + roomRem)

    override fun getOpCode(): Int = 0x1B
    override fun getRawArguments(): IntArray = intArrayOf(chapter, scene / 256, scene % 256, room / 256, room % 256)
}