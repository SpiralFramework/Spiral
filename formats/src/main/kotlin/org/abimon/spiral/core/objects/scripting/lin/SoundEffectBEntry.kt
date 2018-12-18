package org.abimon.spiral.core.objects.scripting.lin

data class SoundEffectBEntry(val arg1: Int, val arg2: Int): LinScript {
    constructor(op: Int, args: IntArray): this(args[0], args[1])

    override val opCode: Int = 0x0B
    override val rawArguments: IntArray = intArrayOf(arg1, arg2)

    override fun format(): String = "SFX B|$arg1, $arg2"
}