package org.abimon.spiral.core.lin.dr1

import org.abimon.spiral.core.lin.LinScript

data class TrialCameraEntry(val charID: Int, val motionID: Int): LinScript {
    constructor(charID: Int, major: Int, rem: Int): this(charID, major * 256 + rem)

    override fun getOpCode(): Int = 0x14
    override fun getRawArguments(): IntArray = intArrayOf(charID, motionID / 256, motionID % 256)
}