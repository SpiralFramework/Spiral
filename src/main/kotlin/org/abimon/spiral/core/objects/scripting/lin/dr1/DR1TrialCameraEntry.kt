package org.abimon.spiral.core.objects.scripting.lin.dr1

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR1TrialCameraEntry(val characterID: Int, val motionID: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], (args[1] shl 8) or args[2])

    override val opCode: Int = 0x14
    override val rawArguments: IntArray = intArrayOf(characterID, motionID shr 8, motionID % 256)
}