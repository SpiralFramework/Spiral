package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1ScreenFlashEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x1F

    val red: Int
        get() = rawArguments[0]

    val green: Int
        get() = rawArguments[1]

    val blue: Int
        get() = rawArguments[2]

    val fadeInDuration: Int
        get() = rawArguments[3]

    val holdDuration: Int
        get() = rawArguments[4]

    val fadeOutDuration: Int
        get() = rawArguments[5]

    val opacity: Int
        get() = rawArguments[6]

    override fun format(): String = "Screen Flash|$red, $green, $blue, $fadeInDuration, $holdDuration, $fadeOutDuration, $opacity"
}