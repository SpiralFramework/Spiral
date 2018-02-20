package org.abimon.spiral.core.objects.scripting.lin.dr1

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR1LoadMapEntry(val room: Int, val state: Int, val padding: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opCode: Int = 0x15
    override val rawArguments: IntArray = intArrayOf(room, state, padding)
}