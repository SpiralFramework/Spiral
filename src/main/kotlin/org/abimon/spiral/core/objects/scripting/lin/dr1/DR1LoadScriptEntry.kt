package org.abimon.spiral.core.objects.scripting.lin.dr1

import org.abimon.spiral.core.objects.scripting.lin.LinScript

data class DR1LoadScriptEntry(val chapter: Int, val scene: Int, val room: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opCode: Int = 0x19
    override val rawArguments: IntArray = intArrayOf(chapter, scene, room)

    override fun format(): String = "Load Script|$chapter, $scene, $room"
}