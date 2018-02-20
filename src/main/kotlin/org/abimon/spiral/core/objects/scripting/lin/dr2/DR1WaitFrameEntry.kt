package org.abimon.spiral.core.objects.scripting.lin.dr2

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR1WaitFrameEntry(val unk1: Int, val unk2: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1])

    override val opCode: Int = 0x3B
    override val rawArguments: IntArray = intArrayOf(unk1, unk2)
}