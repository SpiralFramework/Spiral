package org.abimon.spiral.core.lin.dr2

import org.abimon.spiral.core.lin.LinScript

class WaitFrameDR1Entry(val unk1: Int, val unk2: Int): LinScript {
    
    override fun getOpCode(): Int = 0x3B
    override fun getRawArguments(): IntArray = intArrayOf(unk1, unk2)

    override fun toString(): String = "WaitFrameEntryDR1($unk1, $unk2)"
}