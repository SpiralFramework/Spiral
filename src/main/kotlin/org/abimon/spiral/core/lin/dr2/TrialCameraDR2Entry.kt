package org.abimon.spiral.core.lin.dr2

import org.abimon.spiral.core.lin.LinScript

data class TrialCameraDR2Entry(val charID: Int, val motionID: Int, val unkFirst: Int, val unkSecond: Int, val unkThird: Int): LinScript {
    constructor(charID: Int, major: Int, rem: Int, unkFirst: Int, unkSecond: Int, unkThird: Int): this(charID, major * 256 + rem, unkFirst, unkSecond, unkThird)

    override fun getOpCode(): Int = 0x14
    override fun getRawArguments(): IntArray = intArrayOf(charID, motionID / 256, motionID % 256, unkFirst, unkSecond, unkThird)
}