package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1ScreenFadeEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x22

    val fadeMethod: Int
        get() = rawArguments[0]

    val fadeIn: Boolean
        get() = rawArguments[0] == 0

    val colour: Int
        get() = rawArguments[1]

    /** Duration in frames */
    val duration: Int
        get() = rawArguments[2]

    override fun format(): String = "Screen Fade|$fadeMethod, $colour, $duration"
}