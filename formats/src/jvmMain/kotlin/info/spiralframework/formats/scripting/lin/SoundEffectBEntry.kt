package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class SoundEffectBEntry(val arg1: Int, val arg2: Int): LinEntry {
    constructor(op: Int, args: IntArray): this(args[0], args[1])

    override val opcode: Int = 0x0B
    override val rawArguments: IntArray = intArrayOf(arg1, arg2)

    override fun format(): String = "SFX B|$arg1, $arg2"
}