package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1TrialCameraEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x15

    val characterID: Int
        get() = rawArguments[0]

    val cameraID: Int
        get() = (rawArguments[1] shl 8) or rawArguments[2]

    override fun format(): String = "Trial Camera|$characterID, ${rawArguments[1]}, ${rawArguments[2]}"
}