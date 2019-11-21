package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1SoundEffectAEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x0A

    val sfxID: Int
        get() = (rawArguments[0] shl 8) or rawArguments[1]

    val volume: Int
        get() = rawArguments[2]

    override fun format(): String = "SFX A|${rawArguments[0]}, ${rawArguments[1]}, $volume"
}