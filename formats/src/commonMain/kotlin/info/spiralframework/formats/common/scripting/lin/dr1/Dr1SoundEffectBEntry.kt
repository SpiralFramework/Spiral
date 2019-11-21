package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1SoundEffectBEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x0B

//    val sfxID: Int
//        get() =

    val arg1: Int
        get() = rawArguments[0]

    val arg2: Int
        get() = rawArguments[1]

    override fun format(): String = "SFX B|$arg1, $arg2"
}