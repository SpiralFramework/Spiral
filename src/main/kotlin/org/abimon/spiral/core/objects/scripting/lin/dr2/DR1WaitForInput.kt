package org.abimon.spiral.core.objects.scripting.lin.dr2

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR1WaitForInput(val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2], args[3])

    override val opCode: Int = 0x3A
    override val rawArguments: IntArray = intArrayOf(unk1, unk2, unk3, unk4)
}