package info.spiralframework.formats.scripting.lin.dr2

import info.spiralframework.formats.common.scripting.lin.LinEntry

class DR2LoadScriptEntry(val chapter: Int, val scene: Int, val room: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], (args[1] shl 8) or args[2], (args[3] shl 8) or args[4])

    override val opcode: Int = 0x19
    override val rawArguments: IntArray = intArrayOf(chapter, scene shr 8, scene % 256, room shr 8, room % 256)

    override fun format(): String = "Load Script|$chapter, ${scene shr 8}, ${scene % 256}, ${room shr 8}, ${room % 256}"
}