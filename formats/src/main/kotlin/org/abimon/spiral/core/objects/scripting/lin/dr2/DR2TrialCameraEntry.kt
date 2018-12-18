package org.abimon.spiral.core.objects.scripting.lin.dr2

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR2TrialCameraEntry(val characterID: Int, val motionID: Int, val unk3: Int, val unk4: Int, val unk5: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], (args[1] shl 8) or args[2], args[3], args[4], args[5])

    override val opCode: Int = 0x14
    override val rawArguments: IntArray = intArrayOf(characterID, motionID shr 8, motionID % 256, unk3, unk4, unk5)

    override fun format(): String = "Trial Camera|$characterID, ${motionID shr 8}, ${motionID % 256}, $unk3, $unk4, $unk5"
}