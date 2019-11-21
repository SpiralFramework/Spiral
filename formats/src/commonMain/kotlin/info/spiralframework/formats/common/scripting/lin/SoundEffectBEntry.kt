package info.spiralframework.formats.common.scripting.lin

inline class SoundEffectBEntry(override val rawArguments: IntArray): LinEntry {
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