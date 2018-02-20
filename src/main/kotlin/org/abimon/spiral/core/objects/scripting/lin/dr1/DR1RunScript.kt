package org.abimon.spiral.core.objects.scripting.lin.dr1

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR1RunScript(val chapter: Int, val room: Int, val scene: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opCode: Int = 0x1B
    override val rawArguments: IntArray = intArrayOf(chapter, room, scene)
}