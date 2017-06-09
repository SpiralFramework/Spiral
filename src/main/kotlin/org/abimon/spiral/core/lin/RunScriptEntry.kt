package org.abimon.spiral.core.lin

data class RunScriptEntry(val chapter: Int, val scene: Int, val room: Int): LinScript {
    
    override fun getOpCode(): Int = 0x1B
    override fun getRawArguments(): IntArray = intArrayOf(chapter, scene, room)
}