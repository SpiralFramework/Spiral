package org.abimon.spiral.core.lin

data class SpriteEntry(val objID: Int, val charID: Int, val spriteID: Int, val spriteState: Int, val spriteEntryTransition: Int): LinScript {
    override fun getOpCode(): Int = 0x1E
    override fun getRawArguments(): IntArray = intArrayOf(objID, charID, spriteID, spriteState, spriteEntryTransition)
}