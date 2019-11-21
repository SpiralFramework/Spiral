package info.spiralframework.formats.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class DR1RunScript(val chapter: Int, val room: Int, val scene: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opcode: Int = 0x1B
    override val rawArguments: IntArray = intArrayOf(chapter, room, scene)

    override fun format(): String = "Run Script|$chapter, $room, $scene"
}