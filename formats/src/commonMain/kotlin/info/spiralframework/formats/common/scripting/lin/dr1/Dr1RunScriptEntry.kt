package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1RunScriptEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    val chapter: Int
        get() = rawArguments[0]

    val scene: Int
        get() = rawArguments[1]

    val room: Int
        get() = rawArguments[2]

    override val opcode: Int
        get() = 0x1B

    override fun format(): String = "Run Script|$chapter, $scene, $room"
}