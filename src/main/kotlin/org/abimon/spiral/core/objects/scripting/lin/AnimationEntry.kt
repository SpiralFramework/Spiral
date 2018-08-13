package org.abimon.spiral.core.objects.scripting.lin

data class AnimationEntry(val id: Int, val arg3: Int, val arg4: Int, val arg5: Int, val arg6: Int, val arg7: Int, val frame: Int): LinScript {
    constructor(op: Int, args: IntArray): this((args[0] shl 8) or args[1], args[2], args[3], args[4], args[5], args[6], args[7])

    override val opCode: Int = 0x06
    override val rawArguments: IntArray = intArrayOf(id shr 8, id % 256, arg3, arg4, arg5, arg6, arg7, frame)

    override fun format(): String = "Animation|${id shr 8}, ${id % 256}, $arg3, $arg4, $arg5, $arg6, $arg7, $frame"
}