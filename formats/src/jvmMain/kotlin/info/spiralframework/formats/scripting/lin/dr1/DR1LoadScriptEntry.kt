package info.spiralframework.formats.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class DR1LoadScriptEntry(val chapter: Int, val scene: Int, val room: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opcode: Int = 0x19
    override val rawArguments: IntArray = intArrayOf(chapter, scene, room)

    override fun format(): String = "Load Script|$chapter, $scene, $room"
}