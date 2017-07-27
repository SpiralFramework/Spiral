package org.abimon.spiral.core.lin.dr2

import org.abimon.spiral.core.lin.LinScript

class WaitForInputDR1Entry(val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int) : LinScript {

    override fun getOpCode(): Int = 0x3A
    override fun getRawArguments(): IntArray = intArrayOf(unk1, unk2, unk3, unk4)

    override fun toString(): String = "WaitForInputDR1Entry($unk1, $unk2, $unk3, $unk4)"
}