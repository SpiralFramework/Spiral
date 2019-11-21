package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1LoadScriptEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    val chapter: Int
        get() = rawArguments[0]

    val scene: Int
        get() = rawArguments[1]

    val room: Int
        get() = rawArguments[2]

    override val opcode: Int
        get() = 0x19

    override fun format(): String = "Load Script|$chapter, $scene, $room"
}