package org.abimon.spiral.core.objects.scripting.lin.dr2

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR2LoadMapEntry(val room: Int, val state: Int, val padding: Int, val unk4: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2], args[3])

    override val opCode: Int = 0x15
    override val rawArguments: IntArray = intArrayOf(room, state, padding, unk4)

    override fun format(): String = "Load Map|$room, $state, $padding, $unk4"
}